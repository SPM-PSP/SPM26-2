<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchCategories, fetchProblemSolution } from '@/api/problem'
import {
  adminAddProblem,
  adminAddSolution,
  adminAddTestCase,
  adminDeleteProblem,
  adminDeleteSolution,
  adminDeleteTestCase,
  adminProblemDetail,
  adminUpdateProblem,
  adminUpdateSolution,
  adminUpdateTestCase,
} from '@/api/admin'
import type { AdminProblemDetail, AdminTestCaseItem, CategoryVO, SolutionItem } from '@/types/api'

const route = useRoute()
const router = useRouter()

const isCreate = computed(() => route.name === 'admin-problem-new')
const problemId = computed(() => (isCreate.value ? NaN : Number(route.params.id)))

const categories = ref<CategoryVO[]>([])
const selectedCats = ref<string[]>([])

const title = ref('')
const difficulty = ref('easy')
const description = ref('')
const inputFormat = ref('')
const outputFormat = ref('')
const sampleInput = ref('')
const sampleOutput = ref('')
const timeLimit = ref(1000)
const memoryLimit = ref(65536)

const testCases = ref<AdminTestCaseItem[]>([])
const solutions = ref<SolutionItem[]>([])

const err = ref('')
const msg = ref('')
const saving = ref(false)

const newCaseInput = ref<File | null>(null)
const newCaseOutput = ref<File | null>(null)
const replaceCaseId = ref<number | null>(null)
const repInput = ref<File | null>(null)
const repOutput = ref<File | null>(null)

const solForm = ref({ title: '', content: '', language: 'C++', code: '' })
const editSol = ref<SolutionItem | null>(null)

function toggleCat(name: string) {
  const i = selectedCats.value.indexOf(name)
  if (i >= 0) selectedCats.value.splice(i, 1)
  else selectedCats.value.push(name)
}

async function loadCategories() {
  try {
    const res = await fetchCategories()
    if (res.code === 200) categories.value = res.data ?? []
  } catch {
    /* ignore */
  }
}

async function loadDetail() {
  if (isCreate.value || Number.isNaN(problemId.value)) return
  err.value = ''
  try {
    const res = await adminProblemDetail(problemId.value)
    if (res.code !== 200 || !res.data) {
      err.value = res.message || '加载失败'
      return
    }
    applyDetail(res.data)
    await loadSolutions()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  }
}

function applyDetail(d: AdminProblemDetail) {
  title.value = d.title
  difficulty.value = d.difficulty
  description.value = d.description
  inputFormat.value = d.inputFormat
  outputFormat.value = d.outputFormat
  sampleInput.value = d.sampleInput
  sampleOutput.value = d.sampleOutput
  timeLimit.value = d.timeLimit
  memoryLimit.value = d.memoryLimit
  selectedCats.value = [...(d.categoryNames ?? [])]
  testCases.value = d.testCases ?? []
}

async function loadSolutions() {
  if (isCreate.value) {
    solutions.value = []
    return
  }
  try {
    const res = await fetchProblemSolution(problemId.value)
    if (res.code === 200 && res.data?.solution) {
      solutions.value = res.data.solution
    } else {
      solutions.value = []
    }
  } catch {
    solutions.value = []
  }
}

async function onSave() {
  if (!selectedCats.value.length) {
    err.value = '请至少选择一个题目分类'
    return
  }
  saving.value = true
  err.value = ''
  msg.value = ''
  try {
    if (isCreate.value) {
      const res = await adminAddProblem({
        title: title.value.trim(),
        difficulty: difficulty.value,
        categoryNames: selectedCats.value,
        description: description.value,
        inputFormat: inputFormat.value,
        outputFormat: outputFormat.value,
        sampleInput: sampleInput.value,
        sampleOutput: sampleOutput.value,
        timeLimit: timeLimit.value,
        memoryLimit: memoryLimit.value,
      })
      if (res.code !== 200) {
        err.value = res.message || '保存失败'
        return
      }
      msg.value = res.message || '已创建'
      await router.replace({ name: 'admin-problems' })
    } else {
      const res = await adminUpdateProblem({
        problemId: problemId.value,
        title: title.value.trim(),
        difficulty: difficulty.value,
        categoryNames: selectedCats.value,
        description: description.value,
        inputFormat: inputFormat.value,
        outputFormat: outputFormat.value,
        sampleInput: sampleInput.value,
        sampleOutput: sampleOutput.value,
        timeLimit: timeLimit.value,
        memoryLimit: memoryLimit.value,
      })
      if (res.code !== 200) {
        err.value = res.message || '保存失败'
        return
      }
      msg.value = res.message || '已保存'
      await loadDetail()
    }
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  } finally {
    saving.value = false
  }
}

async function onDeleteProblem() {
  if (isCreate.value) return
  if (!confirm('确定删除该题目及关联数据？')) return
  try {
    const res = await adminDeleteProblem(problemId.value)
    if (res.code !== 200) {
      err.value = res.message || '删除失败'
      return
    }
    await router.replace({ name: 'admin-problems' })
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  }
}

async function onAddCase() {
  if (isCreate.value || !newCaseInput.value || !newCaseOutput.value) {
    err.value = '请先选择 .in 与 .out 文件'
    return
  }
  err.value = ''
  try {
    const res = await adminAddTestCase(problemId.value, newCaseInput.value, newCaseOutput.value)
    if (res.code !== 200) {
      err.value = res.message || '上传失败'
      return
    }
    msg.value = res.message || '用例已添加'
    newCaseInput.value = null
    newCaseOutput.value = null
    await loadDetail()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  }
}

async function onReplaceCase() {
  if (!replaceCaseId.value || !repInput.value || !repOutput.value) {
    err.value = '请选择要替换的用例并上传 .in / .out'
    return
  }
  err.value = ''
  try {
    const res = await adminUpdateTestCase(replaceCaseId.value, repInput.value, repOutput.value)
    if (res.code !== 200) {
      err.value = res.message || '更新失败'
      return
    }
    msg.value = res.message || '用例已更新'
    replaceCaseId.value = null
    repInput.value = null
    repOutput.value = null
    await loadDetail()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  }
}

async function onDeleteCase(id: number) {
  if (!confirm('删除该测试用例？')) return
  try {
    const res = await adminDeleteTestCase(id)
    if (res.code !== 200) {
      err.value = res.message || '删除失败'
      return
    }
    await loadDetail()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  }
}

async function onAddSolution() {
  if (isCreate.value) return
  try {
    const res = await adminAddSolution({
      problemId: problemId.value,
      title: solForm.value.title.trim(),
      content: solForm.value.content,
      language: solForm.value.language,
      code: solForm.value.code,
    })
    if (res.code !== 200) {
      err.value = res.message || '添加失败'
      return
    }
    solForm.value = { title: '', content: '', language: 'C++', code: '' }
    await loadSolutions()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  }
}

async function onSaveSolution() {
  if (!editSol.value) return
  try {
    const res = await adminUpdateSolution({
      solutionId: editSol.value.solutionId,
      title: editSol.value.title,
      content: editSol.value.content,
      language: editSol.value.language,
      code: editSol.value.code,
    })
    if (res.code !== 200) {
      err.value = res.message || '保存失败'
      return
    }
    editSol.value = null
    await loadSolutions()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  }
}

async function onDeleteSolution(id: number) {
  if (!confirm('删除该题解？')) return
  try {
    const res = await adminDeleteSolution(id)
    if (res.code !== 200) {
      err.value = res.message || '删除失败'
      return
    }
    await loadSolutions()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  }
}

onMounted(async () => {
  await loadCategories()
  if (!isCreate.value) await loadDetail()
})

watch(
  () => route.params.id,
  () => {
    if (!isCreate.value) void loadDetail()
  },
)
</script>

<template>
  <div class="page">
    <div class="head">
      <h1>{{ isCreate ? '新增题目' : `编辑题目 #${problemId}` }}</h1>
      <button type="button" class="btn" @click="router.push({ name: 'admin-problems' })">返回列表</button>
    </div>

    <p v-if="err" class="err">{{ err }}</p>
    <p v-if="msg" class="msg">{{ msg }}</p>

    <div class="card">
      <h3>基本信息</h3>
      <div class="field">
        <label>标题</label>
        <input v-model="title" type="text" />
      </div>
      <div class="field">
        <label>难度</label>
        <select v-model="difficulty">
          <option value="easy">easy</option>
          <option value="medium">medium</option>
          <option value="hard">hard</option>
        </select>
      </div>
      <div class="field">
        <label>分类（多选，须为已有分类名）</label>
        <div class="tags">
          <button
            v-for="c in categories"
            :key="c.categoryId"
            type="button"
            class="tag"
            :class="{ on: selectedCats.includes(c.categoryName) }"
            @click="toggleCat(c.categoryName)"
          >
            {{ c.categoryName }}
          </button>
        </div>
      </div>
      <div class="field">
        <label>描述</label>
        <textarea v-model="description" rows="5" />
      </div>
      <div class="field">
        <label>输入格式</label>
        <textarea v-model="inputFormat" rows="3" />
      </div>
      <div class="field">
        <label>输出格式</label>
        <textarea v-model="outputFormat" rows="3" />
      </div>
      <div class="field">
        <label>样例输入</label>
        <textarea v-model="sampleInput" rows="3" />
      </div>
      <div class="field">
        <label>样例输出</label>
        <textarea v-model="sampleOutput" rows="3" />
      </div>
      <div class="row2">
        <div class="field">
          <label>时间限制 (ms)</label>
          <input v-model.number="timeLimit" type="number" min="1" />
        </div>
        <div class="field">
          <label>内存限制 (KB)</label>
          <input v-model.number="memoryLimit" type="number" min="1" />
        </div>
      </div>
      <div class="actions">
        <button type="button" class="btn primary" :disabled="saving" @click="onSave">
          {{ saving ? '保存中…' : '保存' }}
        </button>
        <button v-if="!isCreate" type="button" class="btn danger" @click="onDeleteProblem">删除题目</button>
      </div>
    </div>

    <div v-if="!isCreate" class="card">
      <h3>测试用例</h3>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>输入文件</th>
            <th>输出文件</th>
            <th>创建时间</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="tc in testCases" :key="tc.caseId">
            <td>{{ tc.caseId }}</td>
            <td class="mono">{{ tc.inputUrl }}</td>
            <td class="mono">{{ tc.outputUrl }}</td>
            <td class="muted">{{ tc.createTime }}</td>
            <td>
              <button type="button" class="link" @click="replaceCaseId = tc.caseId">替换文件</button>
              <button type="button" class="link danger" @click="onDeleteCase(tc.caseId)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="replaceCaseId" class="subbox">
        <p>替换用例 #{{ replaceCaseId }}（须为 .in / .out）</p>
        <input type="file" accept=".in" @change="repInput = ($event.target as HTMLInputElement).files?.[0] ?? null" />
        <input type="file" accept=".out" @change="repOutput = ($event.target as HTMLInputElement).files?.[0] ?? null" />
        <button type="button" class="btn primary" @click="onReplaceCase">上传替换</button>
        <button type="button" class="btn" @click="replaceCaseId = null">取消</button>
      </div>

      <div class="subbox">
        <p>新增用例</p>
        <input type="file" accept=".in" @change="newCaseInput = ($event.target as HTMLInputElement).files?.[0] ?? null" />
        <input type="file" accept=".out" @change="newCaseOutput = ($event.target as HTMLInputElement).files?.[0] ?? null" />
        <button type="button" class="btn primary" @click="onAddCase">上传新增</button>
      </div>
    </div>

    <div v-if="!isCreate" class="card">
      <h3>题解</h3>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>语言</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in solutions" :key="s.solutionId">
            <td>{{ s.solutionId }}</td>
            <td>{{ s.title }}</td>
            <td>{{ s.language }}</td>
            <td>
              <button type="button" class="link" @click="editSol = { ...s }">编辑</button>
              <button type="button" class="link danger" @click="onDeleteSolution(s.solutionId)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="editSol" class="subbox">
        <h4>编辑题解 #{{ editSol.solutionId }}</h4>
        <input v-model="editSol.title" type="text" />
        <textarea v-model="editSol.content" rows="3" />
        <input v-model="editSol.language" type="text" />
        <textarea v-model="editSol.code" rows="6" />
        <button type="button" class="btn primary" @click="onSaveSolution">保存题解</button>
        <button type="button" class="btn" @click="editSol = null">取消</button>
      </div>

      <div class="subbox">
        <h4>新增题解</h4>
        <input v-model="solForm.title" type="text" placeholder="标题" />
        <textarea v-model="solForm.content" rows="3" placeholder="内容" />
        <input v-model="solForm.language" type="text" placeholder="语言" />
        <textarea v-model="solForm.code" rows="6" placeholder="代码" />
        <button type="button" class="btn primary" @click="onAddSolution">添加</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  width: 100%;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
h1 {
  margin: 0;
  font-size: 1.2rem;
}
h3 {
  margin: 0 0 12px;
  font-size: 1rem;
}
h4 {
  margin: 0 0 8px;
  font-size: 0.9rem;
}
.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 14px;
}
.field {
  margin-bottom: 12px;
}
.field label {
  display: block;
  margin-bottom: 6px;
  font-size: 0.8rem;
  color: var(--lc-text-muted);
}
.field input,
.field textarea,
.field select {
  width: 100%;
  padding: 8px 10px;
}
.row2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tag {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text-muted);
  cursor: pointer;
  font-size: 0.8rem;
}
.tag.on {
  border-color: var(--lc-accent);
  color: var(--lc-accent);
}
.actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}
.btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text);
  cursor: pointer;
}
.btn.primary {
  background: var(--lc-accent);
  color: #111;
  border: none;
  font-weight: 600;
}
.btn.danger {
  border-color: var(--lc-red);
  color: var(--lc-red);
}
.err {
  color: var(--lc-red);
  font-size: 0.88rem;
}
.msg {
  color: var(--lc-green);
  font-size: 0.88rem;
}
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.8rem;
  margin-bottom: 12px;
}
.table th,
.table td {
  padding: 8px;
  border-bottom: 1px solid var(--lc-border);
  text-align: left;
  vertical-align: top;
}
.table th {
  color: var(--lc-text-muted);
}
.mono {
  word-break: break-all;
  font-family: var(--font-mono);
  font-size: 0.72rem;
}
.muted {
  color: var(--lc-text-muted);
}
.link {
  background: none;
  border: none;
  color: var(--lc-accent);
  cursor: pointer;
  margin-right: 8px;
  font-size: 0.8rem;
}
.link.danger {
  color: var(--lc-red);
}
.subbox {
  margin-top: 12px;
  padding: 12px;
  background: var(--lc-bg);
  border-radius: 8px;
  border: 1px solid var(--lc-border);
}
.subbox input,
.subbox textarea {
  display: block;
  width: 100%;
  margin-bottom: 8px;
  padding: 8px;
}
</style>
