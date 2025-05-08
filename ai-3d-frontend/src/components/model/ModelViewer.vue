<template>
  <div class="model-viewer-container">
    <div class="model-viewer" ref="modelViewerRef">
      <div v-if="loading" class="model-loading">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <div class="mt-2">正在加载3D模型...</div>
      </div>
    </div>
    <div v-if="modelLoaded" class="model-controls mt-3">
      <div class="btn-toolbar" role="toolbar">
        <div class="btn-group me-2" role="group">
          <button class="btn btn-sm btn-outline-primary" @click="resetCamera" title="重置视图">
            <i class="bi bi-arrow-counterclockwise"></i> 重置视图
          </button>
          <button class="btn btn-sm btn-outline-primary" @click="toggleAutoRotate" title="自动旋转">
            <i class="bi" :class="autoRotate ? 'bi-pause-circle' : 'bi-play-circle'"></i>
            {{ autoRotate ? '停止' : '开始' }}旋转
          </button>
        </div>

        <div class="btn-group me-2" role="group">
          <button class="btn btn-sm btn-outline-primary" @click="toggleWireframe" title="网格模式">
            <i class="bi bi-grid-3x3"></i> {{ wireframe ? '隐藏' : '显示' }}网格
          </button>
          <button class="btn btn-sm btn-outline-primary" @click="toggleTexture" title="纹理模式">
            <i class="bi bi-palette"></i> {{ textureVisible ? '隐藏' : '显示' }}纹理
          </button>
        </div>

        <div class="btn-group" role="group">
          <button class="btn btn-sm btn-outline-primary" @click="takeScreenshot" title="截图">
            <i class="bi bi-camera"></i> 截图
          </button>
          <button class="btn btn-sm btn-outline-primary" @click="toggleFullscreen" title="全屏">
            <i class="bi" :class="isFullscreen ? 'bi-fullscreen-exit' : 'bi-fullscreen'"></i>
            {{ isFullscreen ? '退出全屏' : '全屏模式' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'
import { OBJLoader } from 'three/examples/jsm/loaders/OBJLoader'
import { MTLLoader } from 'three/examples/jsm/loaders/MTLLoader'

export default {
  name: 'ModelViewer',
  props: {
    objUrl: {
      type: String,
      required: true
    },
    mtlUrl: {
      type: String,
      default: ''
    },
    textureUrl: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const modelViewerRef = ref(null)
    const loading = ref(true)
    const modelLoaded = ref(false)
    const wireframe = ref(false)
    const autoRotate = ref(false)
    const textureVisible = ref(true)
    const isFullscreen = ref(false)

    // Three.js 变量
    let scene, camera, renderer, controls, model
    let animationFrameId = null
    let loadingManager = null

    // 初始化Three.js场景
    const initScene = () => {
      // 创建加载管理器
      loadingManager = new THREE.LoadingManager()
      loadingManager.onProgress = (url, itemsLoaded, itemsTotal) => {
        console.log(`加载进度: ${Math.round((itemsLoaded / itemsTotal) * 100)}% (${url})`)
      }
      loadingManager.onError = (url) => {
        console.error(`加载错误: ${url}`)
        loading.value = false
      }

      // 创建场景
      scene = new THREE.Scene()
      scene.background = new THREE.Color(0xf0f0f0)

      // 创建相机
      camera = new THREE.PerspectiveCamera(
        45,
        modelViewerRef.value.clientWidth / modelViewerRef.value.clientHeight,
        0.1,
        1000
      )
      camera.position.set(0, 0, 5)

      // 创建渲染器
      renderer = new THREE.WebGLRenderer({ antialias: true })
      renderer.setSize(modelViewerRef.value.clientWidth, modelViewerRef.value.clientHeight)
      renderer.setPixelRatio(window.devicePixelRatio)
      renderer.shadowMap.enabled = true
      modelViewerRef.value.appendChild(renderer.domElement)

      // 创建控制器
      controls = new OrbitControls(camera, renderer.domElement)
      controls.enableDamping = true
      controls.dampingFactor = 0.25
      controls.screenSpacePanning = false
      controls.maxPolarAngle = Math.PI / 2
      controls.minDistance = 1
      controls.maxDistance = 10

      // 添加灯光
      const ambientLight = new THREE.AmbientLight(0xffffff, 0.5)
      scene.add(ambientLight)

      const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
      directionalLight.position.set(1, 1, 1)
      directionalLight.castShadow = true
      scene.add(directionalLight)

      const directionalLight2 = new THREE.DirectionalLight(0xffffff, 0.5)
      directionalLight2.position.set(-1, -1, -1)
      scene.add(directionalLight2)

      // 添加网格辅助线
      const gridHelper = new THREE.GridHelper(10, 10)
      gridHelper.visible = false
      scene.add(gridHelper)

      // 开始动画循环
      animate()

      // 处理窗口大小变化
      window.addEventListener('resize', onWindowResize)
    }

    // 加载模型
    const loadModel = () => {
      loading.value = true
      modelLoaded.value = false

      console.log('开始加载3D模型:')
      console.log('- OBJ URL:', props.objUrl)
      console.log('- MTL URL:', props.mtlUrl)
      console.log('- Texture URL:', props.textureUrl)

      // 检查URL是否有效
      if (!props.objUrl) {
        console.error('模型OBJ URL无效，无法加载模型')
        loading.value = false
        return
      }

      // 如果MTL URL不存在，尝试使用基本材质加载模型
      if (!props.mtlUrl) {
        console.warn('模型MTL URL不存在，将使用基本材质加载模型')
        loadObjWithoutMtl()
        return
      }

      // 加载材质
      const mtlLoader = new MTLLoader(loadingManager)
      mtlLoader.load(
        props.mtlUrl,
        (materials) => {
          materials.preload()

          // 加载OBJ模型
          const objLoader = new OBJLoader(loadingManager)
          objLoader.setMaterials(materials)
          objLoader.load(
            props.objUrl,
            (object) => {
              // 清除之前的模型
              if (model) {
                scene.remove(model)
              }

              // 添加新模型
              model = object

              // 调整模型位置和大小
              const box = new THREE.Box3().setFromObject(model)
              const size = box.getSize(new THREE.Vector3())
              const center = box.getCenter(new THREE.Vector3())

              // 居中模型
              model.position.x = -center.x
              model.position.y = -center.y
              model.position.z = -center.z

              // 缩放模型以适应视图
              const maxDim = Math.max(size.x, size.y, size.z)
              const scale = 3 / maxDim
              model.scale.set(scale, scale, scale)

              // 设置模型接收阴影
              model.traverse((child) => {
                if (child.isMesh) {
                  child.castShadow = true
                  child.receiveShadow = true
                  child.material.wireframe = wireframe.value
                }
              })

              scene.add(model)

              // 更新控制器
              controls.reset()

              // 完成加载
              loading.value = false
              modelLoaded.value = true
            },
            (xhr) => {
              console.log((xhr.loaded / xhr.total) * 100 + '% loaded')
            },
            (error) => {
              console.error('加载OBJ模型失败:', error)
              loading.value = false
              
              // 尝试使用基本材质加载
              loadObjWithoutMtl()
            }
          )
        },
        (xhr) => {
          console.log((xhr.loaded / xhr.total) * 100 + '% MTL loaded')
        },
        (error) => {
          console.error('加载MTL材质失败:', error)
          
          // 尝试使用基本材质加载
          loadObjWithoutMtl()
        }
      )
    }

    // 不使用MTL文件加载模型
    const loadObjWithoutMtl = () => {
      console.log('使用基本材质加载OBJ模型:', props.objUrl)

      // 创建基本材质
      const basicMaterial = new THREE.MeshPhongMaterial({
        color: 0xcccccc,
        shininess: 30,
        side: THREE.DoubleSide
      })

      // 如果有纹理，尝试加载纹理
      if (props.textureUrl) {
        console.log('尝试加载纹理:', props.textureUrl)
        const textureLoader = new THREE.TextureLoader(loadingManager)
        textureLoader.load(
          props.textureUrl,
          (texture) => {
            console.log('纹理加载成功')
            basicMaterial.map = texture
            basicMaterial.needsUpdate = true
          },
          undefined,
          (error) => {
            console.error('加载纹理失败:', error)
          }
        )
      }

      // 加载OBJ模型
      const objLoader = new OBJLoader(loadingManager)
      objLoader.load(
        props.objUrl,
        (object) => {
          // 清除之前的模型
          if (model) {
            scene.remove(model)
          }

          // 添加新模型
          model = object

          // 应用基本材质
          model.traverse((child) => {
            if (child.isMesh) {
              child.material = basicMaterial.clone()
              child.material.wireframe = wireframe.value
              child.castShadow = true
              child.receiveShadow = true
            }
          })

          // 调整模型位置和大小
          const box = new THREE.Box3().setFromObject(model)
          const size = box.getSize(new THREE.Vector3())
          const center = box.getCenter(new THREE.Vector3())

          // 居中模型
          model.position.x = -center.x
          model.position.y = -center.y
          model.position.z = -center.z

          // 缩放模型以适应视图
          const maxDim = Math.max(size.x, size.y, size.z)
          const scale = 3 / maxDim
          model.scale.set(scale, scale, scale)

          scene.add(model)

          // 更新控制器
          controls.reset()

          // 完成加载
          loading.value = false
          modelLoaded.value = true
        },
        (xhr) => {
          console.log((xhr.loaded / xhr.total) * 100 + '% loaded')
        },
        (error) => {
          console.error('加载OBJ模型失败:', error)
          loading.value = false
        }
      )
    }

    // 动画循环
    const animate = () => {
      animationFrameId = requestAnimationFrame(animate)

      // 如果启用了自动旋转
      if (autoRotate.value && model) {
        model.rotation.y += 0.01
      }

      controls.update()
      renderer.render(scene, camera)
    }

    // 处理窗口大小变化
    const onWindowResize = () => {
      if (modelViewerRef.value && camera && renderer) {
        camera.aspect = modelViewerRef.value.clientWidth / modelViewerRef.value.clientHeight
        camera.updateProjectionMatrix()
        renderer.setSize(modelViewerRef.value.clientWidth, modelViewerRef.value.clientHeight)
      }
    }

    // 重置相机位置
    const resetCamera = () => {
      if (controls) {
        controls.reset()
      }
    }

    // 切换线框模式
    const toggleWireframe = () => {
      wireframe.value = !wireframe.value
      if (model) {
        model.traverse((child) => {
          if (child.isMesh) {
            child.material.wireframe = wireframe.value
          }
        })
      }
    }

    // 切换自动旋转
    const toggleAutoRotate = () => {
      autoRotate.value = !autoRotate.value
    }

    // 切换纹理显示
    const toggleTexture = () => {
      textureVisible.value = !textureVisible.value
      if (model) {
        model.traverse((child) => {
          if (child.isMesh) {
            if (!textureVisible.value) {
              // 如果隐藏纹理，使用纯色材质
              child.material.map = null
              child.material.needsUpdate = true
            } else {
              // 如果显示纹理，恢复原始材质
              loadModel()
            }
          }
        })
      }
    }

    // 截图
    const takeScreenshot = () => {
      if (!renderer) return

      // 渲染一帧
      renderer.render(scene, camera)

      // 获取图像数据
      const imgData = renderer.domElement.toDataURL('image/png')

      // 创建下载链接
      const link = document.createElement('a')
      link.href = imgData
      link.download = '3d-model-screenshot.png'
      link.click()
    }

    // 切换全屏
    const toggleFullscreen = () => {
      isFullscreen.value = !isFullscreen.value

      if (isFullscreen.value) {
        // 进入全屏
        if (modelViewerRef.value.requestFullscreen) {
          modelViewerRef.value.requestFullscreen()
        } else if (modelViewerRef.value.webkitRequestFullscreen) {
          modelViewerRef.value.webkitRequestFullscreen()
        } else if (modelViewerRef.value.msRequestFullscreen) {
          modelViewerRef.value.msRequestFullscreen()
        }
      } else {
        // 退出全屏
        if (document.exitFullscreen) {
          document.exitFullscreen()
        } else if (document.webkitExitFullscreen) {
          document.webkitExitFullscreen()
        } else if (document.msExitFullscreen) {
          document.msExitFullscreen()
        }
      }
    }

    // 监听全屏变化
    const handleFullscreenChange = () => {
      isFullscreen.value = !!(
        document.fullscreenElement ||
        document.webkitFullscreenElement ||
        document.msFullscreenElement
      )
    }

    // 监听URL变化，重新加载模型
    watch(
      () => [props.objUrl, props.mtlUrl, props.textureUrl],
      (newValues, oldValues) => {
        console.log('模型属性变化:', {
          newObjUrl: newValues[0],
          oldObjUrl: oldValues[0],
          newMtlUrl: newValues[1],
          oldMtlUrl: oldValues[1],
          newTextureUrl: newValues[2],
          oldTextureUrl: oldValues[2]
        })

        if (scene && newValues[0]) {
          // 如果模型URL变化了，重新加载模型
          if (newValues[0] !== oldValues[0] || newValues[1] !== oldValues[1] || newValues[2] !== oldValues[2]) {
            console.log('模型URL变化，重新加载模型')
            loadModel()
          }
        }
      },
      { deep: true }
    )

    // 组件挂载时初始化
    onMounted(() => {
      if (modelViewerRef.value) {
        initScene()
        if (props.objUrl) {
          loadModel()
        }
      }

      // 添加全屏变化监听
      document.addEventListener('fullscreenchange', handleFullscreenChange)
      document.addEventListener('webkitfullscreenchange', handleFullscreenChange)
      document.addEventListener('msfullscreenchange', handleFullscreenChange)
    })

    // 组件卸载时清理
    onBeforeUnmount(() => {
      if (animationFrameId) {
        cancelAnimationFrame(animationFrameId)
      }

      window.removeEventListener('resize', onWindowResize)

      // 移除全屏变化监听
      document.removeEventListener('fullscreenchange', handleFullscreenChange)
      document.removeEventListener('webkitfullscreenchange', handleFullscreenChange)
      document.removeEventListener('msfullscreenchange', handleFullscreenChange)

      if (renderer) {
        renderer.dispose()
        if (modelViewerRef.value && renderer.domElement) {
          modelViewerRef.value.removeChild(renderer.domElement)
        }
      }

      if (controls) {
        controls.dispose()
      }

      scene = null
      camera = null
      renderer = null
      controls = null
      model = null
    })

    return {
      modelViewerRef,
      loading,
      modelLoaded,
      wireframe,
      autoRotate,
      textureVisible,
      isFullscreen,
      resetCamera,
      toggleWireframe,
      toggleAutoRotate,
      toggleTexture,
      takeScreenshot,
      toggleFullscreen
    }
  }
}
</script>

<style scoped>
.model-viewer-container {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.model-viewer {
  width: 100%;
  height: 400px;
  position: relative;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.model-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #6c757d;
}

.model-controls {
  display: flex;
  justify-content: center;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 0 0 4px 4px;
}

/* 全屏样式 */
.model-viewer:fullscreen {
  width: 100vw;
  height: 100vh;
}

@media (max-width: 767.98px) {
  .model-viewer {
    height: 300px;
  }
  
  .model-controls .btn-group {
    margin-bottom: 5px;
  }
  
  .model-controls .btn-toolbar {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
