document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const uploadForm = document.getElementById('uploadForm');
    const uploadBtn = document.getElementById('uploadBtn');
    const imageFile = document.getElementById('imageFile');
    const syncMode = document.getElementById('syncMode');
    const resultSection = document.getElementById('resultSection');
    const processingInfo = document.getElementById('processingInfo');
    const resultInfo = document.getElementById('resultInfo');
    const errorInfo = document.getElementById('errorInfo');
    const errorMessage = document.getElementById('errorMessage');
    const taskId = document.getElementById('taskId');
    const resultFilesContainer = document.getElementById('resultFilesContainer');
    // 文件链接元素
    const pixelImagesLink = document.getElementById('pixelImagesLink');
    const xyzImagesLink = document.getElementById('xyzImagesLink');
    const objFileLink = document.getElementById('objFileLink');
    const mtlFileLink = document.getElementById('mtlFileLink');
    const textureImageLink = document.getElementById('textureImageLink');

    // 备用链接元素
    const pixelImagesLinkAlt = document.getElementById('pixelImagesLinkAlt');
    const xyzImagesLinkAlt = document.getElementById('xyzImagesLinkAlt');
    const objFileLinkAlt = document.getElementById('objFileLinkAlt');
    const mtlFileLinkAlt = document.getElementById('mtlFileLinkAlt');
    const textureImageLinkAlt = document.getElementById('textureImageLinkAlt');

    // 图片预览元素
    const pixelImagesPreview = document.getElementById('pixelImagesPreview');
    const xyzImagesPreview = document.getElementById('xyzImagesPreview');

    // 3D模型查看器
    const modelViewer = document.getElementById('modelViewer');
    const checkStatusBtn = document.getElementById('checkStatusBtn');
    const connectionStatus = document.getElementById('connectionStatus');
    const checkConnectionBtn = document.getElementById('checkConnectionBtn');
    const initConnectionBtn = document.getElementById('initConnectionBtn');
    const resetConnectionBtn = document.getElementById('resetConnectionBtn');

    // API基础路径
    const API_BASE_URL = '/api';

    // 检查连接状态
    checkConnectionBtn.addEventListener('click', function() {
        checkConnectionStatus();
    });

    // 初始化连接
    initConnectionBtn.addEventListener('click', function() {
        initConnection();
    });

    // 重置连接
    resetConnectionBtn.addEventListener('click', function() {
        resetConnection();
    });

    // 页面加载时自动检查连接状态并尝试初始化连接
    checkConnectionStatus();
    initConnection();

    // 表单提交处理
    uploadForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // 检查是否选择了文件
        if (!imageFile.files || imageFile.files.length === 0) {
            showError('请选择一个图片文件');
            return;
        }

        // 检查文件类型
        const file = imageFile.files[0];
        if (!file.type.startsWith('image/')) {
            showError('请选择有效的图片文件');
            return;
        }

        // 禁用上传按钮
        uploadBtn.disabled = true;
        uploadBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 上传中...';

        // 准备表单数据
        const formData = new FormData();
        formData.append('file', file);

        // 确定上传端点
        const uploadEndpoint = syncMode.checked ?
            `${API_BASE_URL}/reconstruction/upload/sync` :
            `${API_BASE_URL}/reconstruction/upload`;

        // 发送请求
        fetch(uploadEndpoint, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '上传失败');
                });
            }
            return response.json();
        })
        .then(data => {
            if (data.code === 0 && data.data) {
                // 显示结果区域
                resultSection.style.display = 'block';
                resultSection.scrollIntoView({ behavior: 'smooth' });

                // 设置任务ID
                taskId.value = data.data.taskId;

                // 根据状态显示不同信息
                if (data.data.status === 'success') {
                    // 同步模式下，处理已完成
                    processingInfo.style.display = 'none';
                    resultInfo.style.display = 'block';
                    checkStatusBtn.style.display = 'none';

                    // 设置文件链接
                    updateFileLinks(data.data);
                    resultFilesContainer.style.display = 'block';
                } else {
                    // 异步模式下，需要检查状态
                    processingInfo.style.display = 'block';
                    resultInfo.style.display = 'none';
                    resultFilesContainer.style.display = 'none';
                    checkStatusBtn.style.display = 'block';
                }
            } else {
                showError(data.message || '处理失败');
            }
        })
        .catch(error => {
            showError(error.message || '请求失败');
        })
        .finally(() => {
            // 恢复上传按钮
            uploadBtn.disabled = false;
            uploadBtn.innerHTML = '上传并处理';
        });
    });

    // 检查状态按钮点击事件
    checkStatusBtn.addEventListener('click', function() {
        const currentTaskId = taskId.value;
        if (!currentTaskId) {
            showError('任务ID不能为空');
            return;
        }

        // 禁用按钮
        checkStatusBtn.disabled = true;
        checkStatusBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 检查中...';

        // 发送请求
        fetch(`${API_BASE_URL}/reconstruction/status/${currentTaskId}`)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.message || '检查状态失败');
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.code === 0 && data.data) {
                    // 根据状态显示不同信息
                    if (data.data.status === 'success') {
                        // 处理已完成
                        processingInfo.style.display = 'none';
                        resultInfo.style.display = 'block';
                        checkStatusBtn.style.display = 'none';

                        // 设置文件链接
                        updateFileLinks(data.data);
                        resultFilesContainer.style.display = 'block';
                    } else if (data.data.status === 'failed') {
                        // 处理失败
                        processingInfo.style.display = 'none';
                        errorInfo.style.display = 'block';
                        errorMessage.textContent = data.data.errorMessage || '未知错误';
                        resultFilesContainer.style.display = 'none';
                    } else {
                        // 仍在处理中
                        processingInfo.style.display = 'block';
                        resultInfo.style.display = 'none';
                        errorInfo.style.display = 'none';
                        resultFilesContainer.style.display = 'none';
                    }
                } else {
                    showError(data.message || '检查状态失败');
                }
            })
            .catch(error => {
                showError(error.message || '请求失败');
            })
            .finally(() => {
                // 恢复按钮
                checkStatusBtn.disabled = false;
                checkStatusBtn.innerHTML = '检查状态';
            });
    });

    // 检查WebSocket连接状态
    function checkConnectionStatus() {
        // 更新状态显示
        connectionStatus.textContent = '检查中...';
        connectionStatus.className = 'badge bg-info';

        // 禁用按钮
        checkConnectionBtn.disabled = true;

        // 发送请求
        fetch(`${API_BASE_URL}/reconstruction/connection/status`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('请求失败');
                }
                return response.json();
            })
            .then(data => {
                if (data.code === 0) {
                    if (data.data === true) {
                        connectionStatus.textContent = '已连接';
                        connectionStatus.className = 'badge bg-success';
                    } else {
                        connectionStatus.textContent = '未连接';
                        connectionStatus.className = 'badge bg-danger';
                    }
                } else {
                    connectionStatus.textContent = '检查失败';
                    connectionStatus.className = 'badge bg-warning';
                }
            })
            .catch(error => {
                connectionStatus.textContent = '检查失败';
                connectionStatus.className = 'badge bg-warning';
                console.error('检查连接状态失败:', error);
            })
            .finally(() => {
                // 恢复按钮
                checkConnectionBtn.disabled = false;
            });
    }

    // 初始化WebSocket连接
    function initConnection() {
        // 更新状态显示
        connectionStatus.textContent = '连接中...';
        connectionStatus.className = 'badge bg-info';

        // 禁用按钮
        initConnectionBtn.disabled = true;

        // 发送请求
        fetch(`${API_BASE_URL}/reconstruction/connection/init`, {
            method: 'POST'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('请求失败');
                }
                return response.json();
            })
            .then(data => {
                if (data.code === 0) {
                    if (data.data === true) {
                        connectionStatus.textContent = '已连接';
                        connectionStatus.className = 'badge bg-success';
                    } else {
                        connectionStatus.textContent = '连接失败';
                        connectionStatus.className = 'badge bg-danger';
                    }
                } else {
                    connectionStatus.textContent = '连接失败';
                    connectionStatus.className = 'badge bg-danger';
                }
            })
            .catch(error => {
                connectionStatus.textContent = '连接失败';
                connectionStatus.className = 'badge bg-danger';
                console.error('初始化连接失败:', error);
            })
            .finally(() => {
                // 恢复按钮
                initConnectionBtn.disabled = false;
            });
    }

    // 重置WebSocket连接
    function resetConnection() {
        // 确认重置
        if (!confirm('确定要重置WebSocket连接吗？这将关闭当前连接。')) {
            return;
        }

        // 更新状态显示
        connectionStatus.textContent = '重置中...';
        connectionStatus.className = 'badge bg-info';

        // 禁用按钮
        resetConnectionBtn.disabled = true;

        // 发送请求
        fetch(`${API_BASE_URL}/reconstruction/connection/reset`, {
            method: 'POST'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('请求失败');
                }
                return response.json();
            })
            .then(data => {
                if (data.code === 0) {
                    alert('连接已重置，将在下次请求时重新连接。');
                    // 重置后自动检查状态
                    checkConnectionStatus();
                } else {
                    alert('重置失败: ' + (data.message || '未知错误'));
                    connectionStatus.textContent = '重置失败';
                    connectionStatus.className = 'badge bg-warning';
                }
            })
            .catch(error => {
                alert('重置失败: ' + error.message);
                connectionStatus.textContent = '重置失败';
                connectionStatus.className = 'badge bg-warning';
                console.error('重置连接失败:', error);
            })
            .finally(() => {
                // 恢复按钮
                resetConnectionBtn.disabled = false;
            });
    }
    // 显示错误信息
    function showError(message) {
        resultSection.style.display = 'block';
        processingInfo.style.display = 'none';
        resultInfo.style.display = 'none';
        errorInfo.style.display = 'block';
        errorMessage.textContent = message;
        resultFilesContainer.style.display = 'none';
        resultSection.scrollIntoView({ behavior: 'smooth' });
    }

    // Three.js 相关变量
    let scene, camera, renderer, controls;
    let objLoader, mtlLoader;
    let modelLoaded = false;
    const modelLoading = document.getElementById('modelLoading');

    // 初始化 Three.js 场景
    function initThreeJS() {
        try {
            // 创建场景
            scene = new THREE.Scene();
            scene.background = new THREE.Color(0xf0f0f0);

            // 创建相机
            camera = new THREE.PerspectiveCamera(75, modelViewer.clientWidth / modelViewer.clientHeight, 0.1, 1000);
            camera.position.z = 5;

            // 创建渲染器
            renderer = new THREE.WebGLRenderer({ antialias: true });
            renderer.setSize(modelViewer.clientWidth, modelViewer.clientHeight);
            modelViewer.appendChild(renderer.domElement);

            // 添加控制器
            controls = new THREE.OrbitControls(camera, renderer.domElement);
            controls.enableDamping = true;
            controls.dampingFactor = 0.25;

            // 添加光源
            const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
            scene.add(ambientLight);

            const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
            directionalLight.position.set(1, 1, 1);
            scene.add(directionalLight);

            // 创建加载器
            objLoader = new THREE.OBJLoader();
            mtlLoader = new THREE.MTLLoader();

            // 添加网格底板作为参考
            const gridHelper = new THREE.GridHelper(10, 10);
            scene.add(gridHelper);

            // 动画循环
            function animate() {
                requestAnimationFrame(animate);
                if (controls) controls.update();
                renderer.render(scene, camera);
            }

            // 开始动画
            animate();

            // 处理窗口大小变化
            window.addEventListener('resize', function() {
                if (renderer) {
                    camera.aspect = modelViewer.clientWidth / modelViewer.clientHeight;
                    camera.updateProjectionMatrix();
                    renderer.setSize(modelViewer.clientWidth, modelViewer.clientHeight);
                }
            });

            console.log('Three.js scene initialized successfully');
        } catch (error) {
            console.error('Error initializing Three.js scene:', error);
            if (modelLoading) {
                modelLoading.innerHTML = `<div class="text-danger">3D模型查看器初始化失败: ${error.message}</div>`;
            }
        }
    }

    // 加载3D模型
    function loadModel(objUrl, mtlUrl, textureUrl) {
        try {
            console.log('Loading model with:', { objUrl, mtlUrl, textureUrl });

            // 显示加载提示
            if (modelLoading) {
                modelLoading.classList.remove('hidden');
                modelLoading.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">加载中...</span></div><div class="mt-2">正在加载3D模型...</div>';
            }

            // 检查参数
            if (!objUrl || !mtlUrl || !textureUrl) {
                console.error('Missing required URLs:', { objUrl, mtlUrl, textureUrl });
                if (modelLoading) {
                    modelLoading.innerHTML = '<div class="text-danger">缺少必要的模型文件</div>';
                }
                return;
            }

            // 清除现有模型
            scene.traverse(object => {
                if (object.type === 'Mesh') {
                    scene.remove(object);
                }
            });

            // 确保使用预览接口URL
            let previewObjUrl, previewMtlUrl, previewTextureUrl;

            try {
                previewObjUrl = typeof objUrl === 'string' && objUrl.startsWith('ABSOLUTE_PATH:') ?
                    processUrl(objUrl, true) : objUrl;
                previewMtlUrl = typeof mtlUrl === 'string' && mtlUrl.startsWith('ABSOLUTE_PATH:') ?
                    processUrl(mtlUrl, true) : mtlUrl;
                previewTextureUrl = typeof textureUrl === 'string' && textureUrl.startsWith('ABSOLUTE_PATH:') ?
                    processUrl(textureUrl, true) : textureUrl;
            } catch (error) {
                console.error('Error processing URLs:', error);
                if (modelLoading) {
                    modelLoading.innerHTML = '<div class="text-danger">URL处理错误: ' + error.message + '</div>';
                }
                return;
            }

            console.log('Loading MTL from:', previewMtlUrl);
            console.log('Loading OBJ from:', previewObjUrl);
            console.log('Texture URL:', previewTextureUrl);

            // 加载材质和模型
            mtlLoader.load(previewMtlUrl,
                // onLoad回调
                function(materials) {
                    materials.preload();

                    // 如果有纹理图，需要修改材质中的纹理路径
                    if (materials.materials) {
                        Object.values(materials.materials).forEach(material => {
                            if (material.map && material.map.image && material.map.image.src) {
                                // 将纹理路径替换为预览URL
                                material.map.image.src = previewTextureUrl;
                            }
                        });
                    }

                    objLoader.setMaterials(materials);
                    objLoader.load(previewObjUrl,
                        // onLoad回调
                        function(object) {
                            // 调整模型大小和位置
                            let box = new THREE.Box3().setFromObject(object);
                            let size = box.getSize(new THREE.Vector3()).length();
                            let center = box.getCenter(new THREE.Vector3());

                            // 将模型缩放到合适大小
                            let scale = 5 / size;
                            object.scale.set(scale, scale, scale);

                            // 将模型中心移动到原点
                            object.position.x = -center.x * scale;
                            object.position.y = -center.y * scale;
                            object.position.z = -center.z * scale;

                            scene.add(object);
                            modelLoaded = true;

                            // 重置相机位置
                            camera.position.set(0, 0, 5);
                            controls.reset();

                            // 隐藏加载提示
                            if (modelLoading) {
                                modelLoading.classList.add('hidden');
                            }
                        },
                        // onProgress回调
                        function(xhr) {
                            console.log((xhr.loaded / xhr.total * 100) + '% loaded');
                        },
                        // onError回调
                        function(error) {
                            console.error('Error loading OBJ model:', error);
                            if (modelLoading) {
                                modelLoading.innerHTML = '<div class="text-danger">3D模型加载失败: ' + error + '</div>';
                            }
                        }
                );
                },
                // onProgress回调
                function(xhr) {
                    console.log((xhr.loaded / xhr.total * 100) + '% MTL loaded');
                },
                // onError回调
                function(error) {
                    console.error('Error loading MTL file:', error);
                    if (modelLoading) {
                        modelLoading.innerHTML = '<div class="text-danger">材质文件加载失败: ' + error + '</div>';
                    }
                }
            );
        } catch (error) {
            console.error('Error in loadModel:', error);
            if (modelLoading) {
                modelLoading.innerHTML = '<div class="text-danger">加载模型时发生错误: ' + error.message + '</div>';
            }
        }
    }

    // 处理URL路径
    function processUrl(url, isForPreview = false) {
        try {
            console.log('Processing URL:', url, 'isForPreview:', isForPreview);

            // 防止空值
            if (!url) {
                console.error('URL is null or undefined');
                return '';
            }

            // 检查是否是字符串
            if (typeof url !== 'string') {
                console.error('URL is not a string:', url);
                return String(url);
            }

            // 检查是否是带有特殊前缀的绝对路径
            if (url.startsWith('ABSOLUTE_PATH:')) {
                // 移除前缀，获取绝对路径
                const absolutePath = url.substring('ABSOLUTE_PATH:'.length);

                if (isForPreview) {
                    // 对于预览，使用预览接口
                    const previewUrl = API_BASE_URL + '/reconstruction/preview?path=' + encodeURIComponent(absolutePath);
                    console.log('Preview URL:', previewUrl);
                    return previewUrl;
                } else {
                    // 对于下载链接，使用文件协议
                    const cleanPath = absolutePath.replace(/\\/g, '/');
                    const fileUrl = 'file://' + cleanPath;
                    console.log('Converted to file URL:', fileUrl);
                    return fileUrl;
                }
            }

            // 如果是相对路径，添加API基础路径
            const relativeUrl = API_BASE_URL + url;
            console.log('Using relative URL:', relativeUrl);
            return relativeUrl;
        } catch (error) {
            console.error('Error in processUrl:', error);
            return url || '';
        }
    }

    // 更新文件链接和预览
    function updateFileLinks(data) {
        // 初始化 Three.js 场景（如果还没有初始化）
        if (!scene && modelViewer) {
            initThreeJS();
        }

        // 像素图像
        if (data.pixelImagesUrl) {
            const pixelUrl = processUrl(data.pixelImagesUrl);
            pixelImagesLink.href = pixelUrl;
            pixelImagesLinkAlt.href = pixelUrl;
            pixelImagesLink.style.display = 'inline-block';
            pixelImagesLinkAlt.style.display = 'block';

            // 设置图片预览
            pixelImagesPreview.src = processUrl(data.pixelImagesUrl, true);
            pixelImagesPreview.style.display = 'block';
        } else {
            pixelImagesLink.style.display = 'none';
            pixelImagesLinkAlt.style.display = 'none';
            pixelImagesPreview.style.display = 'none';
        }

        // XYZ图像
        if (data.xyzImagesUrl) {
            const xyzUrl = processUrl(data.xyzImagesUrl);
            xyzImagesLink.href = xyzUrl;
            xyzImagesLinkAlt.href = xyzUrl;
            xyzImagesLink.style.display = 'inline-block';
            xyzImagesLinkAlt.style.display = 'block';

            // 设置图片预览
            xyzImagesPreview.src = processUrl(data.xyzImagesUrl, true);
            xyzImagesPreview.style.display = 'block';
        } else {
            xyzImagesLink.style.display = 'none';
            xyzImagesLinkAlt.style.display = 'none';
            xyzImagesPreview.style.display = 'none';
        }

        // OBJ文件
        let objUrl = null;
        if (data.objFileUrl) {
            objUrl = processUrl(data.objFileUrl);
            objFileLink.href = objUrl;
            objFileLinkAlt.href = objUrl;
            objFileLink.style.display = 'inline-block';
            objFileLinkAlt.style.display = 'block';
        } else {
            objFileLink.style.display = 'none';
            objFileLinkAlt.style.display = 'none';
        }

        // MTL文件
        let mtlUrl = null;
        if (data.mtlFileUrl) {
            mtlUrl = processUrl(data.mtlFileUrl);
            mtlFileLink.href = mtlUrl;
            mtlFileLinkAlt.href = mtlUrl;
            mtlFileLink.style.display = 'inline-block';
            mtlFileLinkAlt.style.display = 'block';
        } else {
            mtlFileLink.style.display = 'none';
            mtlFileLinkAlt.style.display = 'none';
        }

        // 纹理图像
        let textureUrl = null;
        if (data.textureImageUrl) {
            textureUrl = processUrl(data.textureImageUrl);
            textureImageLink.href = textureUrl;
            textureImageLinkAlt.href = textureUrl;
            textureImageLink.style.display = 'inline-block';
            textureImageLinkAlt.style.display = 'block';
        } else {
            textureImageLink.style.display = 'none';
            textureImageLinkAlt.style.display = 'none';
        }

        // 如果所有必要的文件都存在，加载3D模型
        if (data.objFileUrl && data.mtlFileUrl && data.textureImageUrl) {
            // 重置模型加载状态
            modelLoaded = false;
            // 传递原始 URL，而不是处理后的 URL
            loadModel(data.objFileUrl, data.mtlFileUrl, data.textureImageUrl);
        } else if (modelLoading) {
            // 如果缺少必要文件，显示错误提示
            modelLoading.classList.remove('hidden');
            modelLoading.innerHTML = '<div class="text-warning">缺少必要的模型文件</div>';
        }
    }
});
