<!-- src/components/FileUpload.vue -->
<template>
  <div class="file-upload">
    <n-upload
        ref="uploadRef"
        :max="maxFiles"
        :file-list="fileList"
        :default-upload="false"
        :show-file-list="showFileList"
        :accept="acceptedTypes"
        :disabled="disabled || uploading"
        @change="handleFileChange"
        @remove="handleFileRemove"
        @before-upload="handleBeforeUpload"
    >
      <n-upload-dragger v-if="!uploading">
        <div class="upload-content">
          <div class="upload-icon">
            <span class="text-4xl">📁</span>
          </div>
          <n-text style="font-size: 16px">
            Нажмите или перетащите файлы в эту область для загрузки
          </n-text>
          <n-p depth="3" style="margin: 8px 0 0 0">
            Допустимые типы: {{ acceptedTypesDisplay }}. Максимальный размер: {{ formatFileSize(maxFileSize) }}
          </n-p>
        </div>
      </n-upload-dragger>
      
      <div v-else class="uploading-state">
        <n-spin size="large" />
        <n-text style="margin-top: 16px">Загрузка файлов...</n-text>
      </div>
    </n-upload>

    <!-- Прогресс загрузки -->
    <div v-if="uploading && uploadProgress > 0" class="upload-progress">
      <n-progress
          :percentage="uploadProgress"
          :status="uploadProgress === 100 ? 'success' : 'default'"
          :show-indicator="true"
      />
      <n-text depth="3" style="margin-top: 8px">
        {{ uploadProgress === 100 ? 'Обработка файла...' : `Загружено: ${uploadProgress}%` }}
      </n-text>
    </div>

    <!-- Список загруженных файлов -->
    <div v-if="uploadedFiles.length > 0" class="uploaded-files">
      <n-divider>Загруженные файлы</n-divider>
      <div class="file-list">
        <div
            v-for="file in uploadedFiles"
            :key="file.id"
            class="file-item"
        >
          <div class="file-info">
            <span class="file-icon">{{ getFileIcon(file.type) }}</span>
            <div class="file-details">
              <n-text class="file-name">{{ file.name }}</n-text>
              <n-text depth="3" class="file-size">{{ formatFileSize(file.size) }}</n-text>
            </div>
          </div>
          <div class="file-actions">
            <n-button
                v-if="file.url"
                text
                type="primary"
                @click="downloadFile(file)"
            >
              Скачать
            </n-button>
            <n-button
                text
                type="error"
                @click="removeFile(file)"
            >
              Удалить
            </n-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Ошибки загрузки -->
    <div v-if="errors.length > 0" class="upload-errors">
      <n-alert
          v-for="(error, index) in errors"
          :key="index"
          type="error"
          :title="error.file?.name || 'Ошибка загрузки'"
          closable
          @close="removeError(index)"
      >
        {{ error.message }}
      </n-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import {
  NUpload,
  NUploadDragger,
  NText,
  NP,
  NSpin,
  NProgress,
  NDivider,
  NButton,
  NAlert,
  useMessage
} from 'naive-ui';
import { fileService } from '@/services/api';
import { handleError } from '@/services/errorHandler';

const props = defineProps({
  // Максимальное количество файлов
  maxFiles: {
    type: Number,
    default: 5
  },
  // Максимальный размер файла в байтах
  maxFileSize: {
    type: Number,
    default: 10 * 1024 * 1024 // 10MB
  },
  // Допустимые типы файлов
  acceptedTypes: {
    type: String,
    default: 'image/*,application/pdf,.doc,.docx,.txt'
  },
  // Показывать список файлов
  showFileList: {
    type: Boolean,
    default: true
  },
  // Заблокировать загрузку
  disabled: {
    type: Boolean,
    default: false
  },
  // Множественная загрузка
  multiple: {
    type: Boolean,
    default: true
  },
  // Предварительно загруженные файлы
  modelValue: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits([
  'update:modelValue',
  'upload-success',
  'upload-error',
  'file-removed'
]);

const message = useMessage();
const uploadRef = ref(null);
const fileList = ref([]);
const uploading = ref(false);
const uploadProgress = ref(0);
const uploadedFiles = ref([]);
const errors = ref([]);

// Вычисляемые свойства
const acceptedTypesDisplay = computed(() => {
  return props.acceptedTypes
    .split(',')
    .map(type => type.trim())
    .filter(type => !type.startsWith('.'))
    .map(type => {
      if (type === 'image/*') return 'изображения';
      if (type === 'application/pdf') return 'PDF';
      if (type.includes('document')) return 'документы';
      return type;
    })
    .join(', ');
});

// Наблюдаем за изменениями modelValue
watch(() => props.modelValue, (newValue) => {
  uploadedFiles.value = newValue || [];
}, { immediate: true });

// Методы
function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function getFileIcon(fileType) {
  if (fileType?.startsWith('image/')) return '🖼️';
  if (fileType === 'application/pdf') return '📄';
  if (fileType?.includes('document')) return '📝';
  if (fileType?.includes('text')) return '📃';
  return '📎';
}

function handleBeforeUpload({ file }) {
  // Проверка размера файла
  if (file.file?.size > props.maxFileSize) {
    message.error(`Файл ${file.name} слишком большой. Максимальный размер: ${formatFileSize(props.maxFileSize)}`);
    return false;
  }

  // Проверка типа файла
  const acceptedTypesArray = props.acceptedTypes.split(',').map(type => type.trim());
  const fileType = file.file?.type;
  const fileName = file.name;
  
  const isAccepted = acceptedTypesArray.some(type => {
    if (type.startsWith('.')) {
      return fileName.endsWith(type);
    }
    if (type.endsWith('/*')) {
      return fileType?.startsWith(type.slice(0, -2));
    }
    return fileType === type;
  });

  if (!isAccepted) {
    message.error(`Файл ${file.name} имеет неподдерживаемый тип`);
    return false;
  }

  return true;
}

function handleFileChange({ fileList: newFileList }) {
  fileList.value = newFileList;
  
  // Автоматически загружаем файлы
  const filesToUpload = newFileList.filter(file => file.status === 'pending');
  if (filesToUpload.length > 0) {
    uploadFiles(filesToUpload);
  }
}

function handleFileRemove({ file, index }) {
  // Удаляем файл из списка
  const fileIndex = fileList.value.findIndex(f => f.id === file.id);
  if (fileIndex > -1) {
    fileList.value.splice(fileIndex, 1);
  }
}

async function uploadFiles(files) {
  if (files.length === 0) return;

  uploading.value = true;
  uploadProgress.value = 0;
  errors.value = [];

  try {
    if (files.length === 1) {
      // Одиночная загрузка
      await uploadSingleFile(files[0]);
    } else {
      // Множественная загрузка
      await uploadMultipleFiles(files);
    }
  } catch (error) {
    handleError(error, {
      context: 'File upload',
      showMessage: false,
    });
    
    errors.value.push({
      file: files[0],
      message: 'Ошибка загрузки файла'
    });
    
    emit('upload-error', error);
  } finally {
    uploading.value = false;
    uploadProgress.value = 0;
  }
}

async function uploadSingleFile(fileWrapper) {
  const file = fileWrapper.file;
  
  try {
    const response = await fileService.uploadFile(file, (progressEvent) => {
      const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
      uploadProgress.value = progress;
    });

    const uploadedFile = {
      id: response.data.id,
      name: file.name,
      size: file.size,
      type: file.type,
      url: response.data.url,
      ...response.data
    };

    uploadedFiles.value.push(uploadedFile);
    emit('update:modelValue', uploadedFiles.value);
    emit('upload-success', uploadedFile);
    
    message.success(`Файл ${file.name} успешно загружен`);
  } catch (error) {
    errors.value.push({
      file: fileWrapper,
      message: error.response?.data?.message || 'Ошибка загрузки файла'
    });
    throw error;
  }
}

async function uploadMultipleFiles(files) {
  const fileObjects = files.map(f => f.file);
  
  try {
    const response = await fileService.uploadMultipleFiles(fileObjects, (progressEvent) => {
      const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
      uploadProgress.value = progress;
    });

    const newUploadedFiles = response.data.map(fileData => ({
      ...fileData,
      url: fileService.getFileUrl(fileData.id)
    }));

    uploadedFiles.value.push(...newUploadedFiles);
    emit('update:modelValue', uploadedFiles.value);
    emit('upload-success', newUploadedFiles);
    
    message.success(`${files.length} файлов успешно загружено`);
  } catch (error) {
    errors.value.push({
      message: error.response?.data?.message || 'Ошибка загрузки файлов'
    });
    throw error;
  }
}

function downloadFile(file) {
  if (file.url) {
    window.open(file.url, '_blank');
  }
}

async function removeFile(file) {
  try {
    if (file.id) {
      await fileService.deleteFile(file.id);
    }
    
    const index = uploadedFiles.value.findIndex(f => f.id === file.id);
    if (index > -1) {
      uploadedFiles.value.splice(index, 1);
      emit('update:modelValue', uploadedFiles.value);
      emit('file-removed', file);
    }
    
    message.success(`Файл ${file.name} удален`);
  } catch (error) {
    handleError(error, {
      context: 'File removal',
      customMessage: 'Ошибка удаления файла',
    });
  }
}

function removeError(index) {
  errors.value.splice(index, 1);
}

// Публичные методы
defineExpose({
  clearFiles: () => {
    fileList.value = [];
    uploadedFiles.value = [];
    errors.value = [];
    emit('update:modelValue', []);
  },
  getUploadedFiles: () => uploadedFiles.value,
  uploadFiles: (files) => uploadFiles(files)
});
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.upload-content {
  text-align: center;
  padding: 40px 20px;
}

.upload-icon {
  margin-bottom: 16px;
}

.uploading-state {
  text-align: center;
  padding: 40px 20px;
}

.upload-progress {
  margin-top: 16px;
}

.uploaded-files {
  margin-top: 24px;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid var(--n-border-color);
  border-radius: 6px;
  background-color: var(--n-card-color);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.file-icon {
  font-size: 20px;
}

.file-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-name {
  font-weight: 500;
}

.file-size {
  font-size: 12px;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.upload-errors {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>