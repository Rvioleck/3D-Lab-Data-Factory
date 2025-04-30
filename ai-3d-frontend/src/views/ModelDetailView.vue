<template>
  <div class="model-detail">
    <div v-if="loading" class="loading">
      加载中...
    </div>

    <div v-else-if="model" class="model-container">
      <h2>{{ model.name || '未命名模型' }}</h2>

      <div class="model-viewer">
        <ModelViewer
          :model-url="model.modelUrl"
          :thumbnail-url="model.thumbnailUrl"
        />
      </div>

      <div class="model-info">
        <p>创建时间: {{ new Date(model.createTime).toLocaleString() }}</p>
        <p>状态: {{ model.status }}</p>

        <div class="source-image" v-if="model.sourceImageUrl">
          <h3>源图片:</h3>
          <img :src="model.sourceImageUrl" alt="源图片">
        </div>
      </div>

      <div class="actions">
        <button
          class="btn btn-danger"
          @click="deleteModel"
        >
          删除模型
        </button>
      </div>
    </div>

    <div v-else class="error">
      模型不存在或已被删除
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Modal } from 'bootstrap'
import { cleanupModalState } from '@/utils/modalHelper'
import ModelViewer from '@/components/model/ModelViewer.vue'
import { reconstructionApi } from '@/api/reconstruction'

const API_URL = import.meta.env.VITE_API_URL || ''

const loading = ref(false)
const model = ref(null)
const route = useRoute()
const router = useRouter()

const modelId = computed(() => route.params.id)

const loadModelDetail = async () => {
  loading.value = true
  try {
    const response = await reconstructionApi.getModelById(modelId.value)
    if (response.code === 0 && response.data) {
      model.value = {
        ...response.data,
        sourceImageUrl: response.data.sourceImageId
          ? `${API_URL}/picture/${response.data.sourceImageId}`
          : null
      }
    } else {
      console.error('获取模型失败:', response.message)
      model.value = null
    }
  } catch (error) {
    console.error('加载模型详情失败:', error)
    model.value = null
  } finally {
    loading.value = false
  }
}

const deleteModel = async () => {
  try {
    await reconstructionApi.deleteModel(modelId.value)
    router.push('/models')
  } catch (error) {
    console.error('删除模型失败:', error)
  }
}

onMounted(() => {
  loadModelDetail()

  // 初始化时清除模态框背景
  cleanupModalState()
})
</script>

<style scoped>
.model-detail {
  padding: 20px;
}

.model-container {
  max-width: 1200px;
  margin: 0 auto;
}

.model-viewer {
  margin: 20px 0;
  height: 500px;
  border: 1px solid #ddd;
}

.model-info {
  margin: 20px 0;
}

.source-image {
  margin-top: 20px;

  img {
    max-width: 300px;
    border: 1px solid #ddd;
  }
}

.actions {
  margin-top: 20px;
}

.loading, .error {
  text-align: center;
  padding: 40px;
  color: #666;
}
</style>
