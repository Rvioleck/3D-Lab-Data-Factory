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
      <div class="btn-group" role="group">
        <button class="btn btn-sm btn-outline-primary" @click="resetCamera">
          <i class="bi bi-arrow-counterclockwise"></i> 重置视图
        </button>
        <button class="btn btn-sm btn-outline-primary" @click="toggleWireframe">
          <i class="bi bi-grid-3x3"></i> {{ wireframe ? '隐藏' : '显示' }}网格
        </button>
        <button class="btn btn-sm btn-outline-primary" @click="toggleAutoRotate">
          <i class="bi" :class="autoRotate ? 'bi-pause-circle' : 'bi-play-circle'"></i>
          {{ autoRotate ? '停止' : '开始' }}旋转
        </button>
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
      required: true
    },
    textureUrl: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const modelViewerRef = ref(null)
    const loading = ref(true)
    const modelLoaded = ref(false)
    const wireframe = ref(false)
    const autoRotate = ref(false)

    // Three.js 变量
    let scene, camera, renderer, controls, model
    let animationFrameId = null

    // 初始化Three.js场景
    const initScene = () => {
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
      camera.position.z = 5

      // 创建渲染器
      renderer = new THREE.WebGLRenderer({ antialias: true })
      renderer.setSize(modelViewerRef.value.clientWidth, modelViewerRef.value.clientHeight)
      renderer.setPixelRatio(window.devicePixelRatio)
      modelViewerRef.value.appendChild(renderer.domElement)

      // 创建控制器
      controls = new OrbitControls(camera, renderer.domElement)
      controls.enableDamping = true
      controls.dampingFactor = 0.05

      // 添加灯光
      const ambientLight = new THREE.AmbientLight(0xffffff, 0.5)
      scene.add(ambientLight)

      const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
      directionalLight.position.set(1, 1, 1)
      scene.add(directionalLight)

      // 添加辅助网格
      const gridHelper = new THREE.GridHelper(10, 10)
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

      // 加载材质
      const mtlLoader = new MTLLoader()
      mtlLoader.load(
        props.mtlUrl,
        (materials) => {
          materials.preload()

          // 加载OBJ模型
          const objLoader = new OBJLoader()
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
        },
        (xhr) => {
          console.log((xhr.loaded / xhr.total) * 100 + '% MTL loaded')
        },
        (error) => {
          console.error('加载MTL材质失败:', error)
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

    // 监听URL变化，重新加载模型
    watch(
      () => [props.objUrl, props.mtlUrl, props.textureUrl],
      () => {
        if (scene && props.objUrl && props.mtlUrl) {
          loadModel()
        }
      }
    )

    // 组件挂载时初始化
    onMounted(() => {
      if (modelViewerRef.value) {
        initScene()
        if (props.objUrl && props.mtlUrl) {
          loadModel()
        }
      }
    })

    // 组件卸载时清理
    onBeforeUnmount(() => {
      if (animationFrameId) {
        cancelAnimationFrame(animationFrameId)
      }
      
      window.removeEventListener('resize', onWindowResize)
      
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
      resetCamera,
      toggleWireframe,
      toggleAutoRotate
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
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: rgba(240, 240, 240, 0.8);
  z-index: 10;
}

.model-controls {
  display: flex;
  justify-content: center;
}
</style>
