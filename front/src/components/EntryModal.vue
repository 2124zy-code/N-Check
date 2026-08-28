<template>
  <a-modal
    v-model:open="modalOpen"
    :title="editData ? '编辑题目详情' : '录入新面试题'"
    :footer="null"
    :width="900"
    :destroy-on-close="true"
    @cancel="emit('update:open', false)"
  >
    <a-form :model="form" layout="vertical" @finish="handleSubmit" class="entry-modal-form">
      <!-- 1. Content Type Switcher -->
      <a-form-item label="内容类型" required>
        <div class="content-type-grid">
          <div
            class="type-selection-card"
            :class="{ 'card-bagu-active': form.type === '八股文' }"
            @click="switchType('八股文')"
          >
            <div class="tsc-icon-circle tsc-blue">
              <ReadOutlined />
            </div>
            <div class="tsc-text">
              <div class="tsc-name">八股文理论考点</div>
              <div class="tsc-desc">核心原理解析 · 架构设计 · 追问避坑</div>
            </div>
          </div>

          <div
            class="type-selection-card"
            :class="{ 'card-algo-active': form.type === '算法题' }"
            @click="switchType('算法题')"
          >
            <div class="tsc-icon-circle tsc-green">
              <CodeOutlined />
            </div>
            <div class="tsc-text">
              <div class="tsc-name">算法手撕题解</div>
              <div class="tsc-desc">LeetCode模式 / ACM模式 · 实时行内高亮编辑器</div>
            </div>
          </div>
        </div>
      </a-form-item>

      <!-- 2. Title Input -->
      <a-form-item
        label="题目标题 / 核心考察点"
        name="title"
        :rules="[{ required: true, message: '请输入题目标题或考察知识点' }]"
      >
        <a-input
          v-model:value="form.title"
          size="large"
          :placeholder="form.type === '八股文' ? '例如：深入剖析 MySQL InnoDB 事务隔离级别与 MVCC 实现机制' : '例如：无重复字符的最长子串 / A+B 笔试题'"
          allow-clear
        />
      </a-form-item>

      <!-- 3. Difficulty & Mastery Status Chips -->
      <div class="dual-row-layout">
        <!-- Difficulty -->
        <div class="dual-col-item">
          <label class="item-label">难度级别</label>
          <div class="chips-container">
            <div
              v-for="d in ['简单', '中等', '困难']"
              :key="d"
              class="selection-chip"
              :class="{ 'chip-selected': form.difficulty === d, [`diff-${d}`]: true }"
              @click="form.difficulty = d"
            >
              <FireOutlined v-if="d === '困难'" style="margin-right: 3px;" />
              {{ d }}
            </div>
          </div>
        </div>

        <!-- Mastery Status -->
        <div class="dual-col-item">
          <label class="item-label">掌握熟练度</label>
          <div class="chips-container">
            <div
              v-for="s in ['未掌握', '学习中', '已掌握']"
              :key="s"
              class="selection-chip"
              :class="{ 'chip-selected': form.status === s, [`status-${s}`]: true }"
              @click="form.status = s"
            >
              <CheckCircleOutlined v-if="s === '已掌握'" style="margin-right: 3px;" />
              {{ s }}
            </div>
          </div>
        </div>
      </div>

      <!-- 4. Tags Section -->
      <a-form-item label="标签分类体系">
        <div class="tag-management-card">
          <div class="current-tags-list">
            <a-tag
              v-for="t in form.tags"
              :key="t"
              closable
              color="processing"
              @close="removeTag(t)"
              class="active-tag-pill"
            >{{ t }}</a-tag>

            <a-input
              v-if="tagVisible"
              ref="tagInputRef"
              v-model:value="tagInput"
              size="small"
              style="width: 110px"
              placeholder="输入标签"
              @blur="confirmTag"
              @keyup.enter="confirmTag"
            />
            <span v-else class="add-custom-tag-btn" @click="showTagInput">
              <PlusOutlined /> 自定义标签
            </span>
          </div>

          <div class="preset-tags-row">
            <span class="ptr-title">快捷推荐：</span>
            <div class="ptr-chips">
              <span
                v-for="p in currentPresets"
                :key="p"
                class="ptr-item"
                :class="{ 'ptr-active': form.tags.includes(p) }"
                @click="togglePreset(p)"
              >
                {{ p }}
              </span>
            </div>
          </div>
        </div>
      </a-form-item>

      <!-- 5. Content Area -->

      <!-- Mode A: 算法题专属专业实时行内高亮编辑器 (CodeMirror 6) -->
      <div v-if="form.type === '算法题'" class="algo-dedicated-section">
        <!-- 题解思路说明 -->
        <a-form-item label="1. 算法思路与复杂度分析 (Markdown)">
          <a-textarea
            v-model:value="algoAnalysis"
            :rows="3"
            placeholder="说明核心思路（如：时间复杂度 O(N)，空间复杂度 O(1)...）"
            class="algo-analysis-input"
          />
        </a-form-item>

        <!-- 真正的实时行内高亮代码编辑器 (打字即高亮) -->
        <a-form-item label="2. 算法代码（实时高亮编写）">
          <div class="codemirror-editor-wrapper">
            <!-- Header Toolbar -->
            <div class="cme-header">
              <div class="cme-left">
                <!-- Mac dots -->
                <span class="mac-dot red-dot"></span>
                <span class="mac-dot yellow-dot"></span>
                <span class="mac-dot green-dot"></span>

                <!-- Mode Capsule (LeetCode vs ACM) -->
                <div class="mode-capsule">
                  <button
                    type="button"
                    class="mode-btn"
                    :class="{ active: algoMode === 'leetcode' }"
                    @click="algoMode = 'leetcode'"
                  >
                    LeetCode 核心模式
                  </button>
                  <button
                    type="button"
                    class="mode-btn"
                    :class="{ active: algoMode === 'acm' }"
                    @click="algoMode = 'acm'"
                  >
                    ACM 笔试输入输出模式
                  </button>
                </div>

                <!-- Language Selector -->
                <a-select
                  v-model:value="codeLang"
                  size="small"
                  class="code-lang-selector"
                  :dropdown-match-select-width="false"
                >
                  <a-select-option value="java">Java</a-select-option>
                  <a-select-option value="cpp">C++</a-select-option>
                  <a-select-option value="python">Python</a-select-option>
                  <a-select-option value="go">Go</a-select-option>
                  <a-select-option value="javascript">JavaScript</a-select-option>
                  <a-select-option value="typescript">TypeScript</a-select-option>
                  <a-select-option value="rust">Rust</a-select-option>
                  <a-select-option value="sql">SQL</a-select-option>
                </a-select>
              </div>

              <!-- Right Tools -->
              <div class="cme-right">
                <button type="button" class="tool-action-btn" @click="insertTemplate">
                  <ThunderboltOutlined /> 填入{{ algoMode === 'leetcode' ? 'LeetCode' : 'ACM' }}模板
                </button>
                <button type="button" class="tool-action-btn" @click="clearCode" v-if="algoCode">
                  清空代码
                </button>
              </div>
            </div>

            <!-- Mode Description Bar -->
            <div class="mode-desc-bar">
              <span v-if="algoMode === 'leetcode'">
                💡 <strong>LeetCode 核心代码模式</strong>：仅需实现核心函数，参数已由平台传入，通过 <code>return</code> 返回答案。
              </span>
              <span v-else>
                💡 <strong>ACM 笔试模式</strong>：包含 <code>main</code> 函数入口与导包，需从标准输入（<code>stdin</code>）读取并打印到控制台（<code>stdout</code>）。
              </span>
            </div>

            <!-- Real-time In-place Live Code Editor Component -->
            <div class="editor-cm-container">
              <codemirror
                v-model="algoCode"
                :placeholder="`// 在此编写或粘贴 ${codeLang.toUpperCase()} 代码（或点击上方【填入模板】）...`"
                :style="{ height: '300px' }"
                :autofocus="false"
                :indent-with-tab="true"
                :tab-size="4"
                :extensions="cmExtensions"
              />
            </div>
          </div>
        </a-form-item>
      </div>

      <!-- Mode B: 八股文常规 Markdown 编辑器 -->
      <div v-else class="bagu-dedicated-section">
        <a-form-item label="知识点详细解析（支持 Markdown 语法与代码）">
          <div class="markdown-editor-wrapper">
            <div class="editor-header-bar">
              <div class="syntax-helpers">
                <button type="button" class="helper-tool-btn" @click="insertSnippet('code')"><CodeOutlined /> 插入代码块</button>
                <button type="button" class="helper-tool-btn" @click="insertSnippet('h2')">二级标题</button>
                <button type="button" class="helper-tool-btn" @click="insertSnippet('list')">列表要点</button>
              </div>
              <div class="mode-tab-group">
                <button
                  type="button"
                  class="mode-switch-btn"
                  :class="{ active: mode === 'edit' }"
                  @click="mode = 'edit'"
                >
                  <EditOutlined /> 编辑模式
                </button>
                <button
                  type="button"
                  class="mode-switch-btn"
                  :class="{ active: mode === 'preview' }"
                  @click="mode = 'preview'"
                >
                  <EyeOutlined /> 渲染预览
                </button>
              </div>
            </div>

            <a-textarea
              v-if="mode === 'edit'"
              v-model:value="form.content"
              :rows="12"
              :placeholder="baguTpl"
              class="editor-textarea-field"
            />
            <div v-else class="editor-preview-field" v-html="renderBaguMd(form.content)"></div>
          </div>
        </a-form-item>
      </div>

      <!-- Modal Footer -->
      <div class="modal-bottom-actions">
        <a-button size="large" @click="emit('update:open', false)">取消</a-button>
        <a-button type="primary" size="large" html-type="submit" class="submit-btn">
          {{ editData ? '保存修改' : '确认录入题库' }}
        </a-button>
      </div>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, watch, nextTick, computed } from 'vue'
import {
  PlusOutlined,
  ReadOutlined,
  CodeOutlined,
  FireOutlined,
  CheckCircleOutlined,
  EditOutlined,
  EyeOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { Codemirror } from 'vue-codemirror'
import { java } from '@codemirror/lang-java'
import { cpp } from '@codemirror/lang-cpp'
import { python } from '@codemirror/lang-python'
import { javascript } from '@codemirror/lang-javascript'
import { rust } from '@codemirror/lang-rust'
import { sql } from '@codemirror/lang-sql'
import { oneDark } from '@codemirror/theme-one-dark'
import hljs from 'highlight.js/lib/core'
import hljsJava from 'highlight.js/lib/languages/java'
import 'highlight.js/styles/atom-one-dark.css'
import { useInterviewStore } from '../stores/useInterviewStore'

hljs.registerLanguage('java', hljsJava)

const props = defineProps({
  open: Boolean,
  companyId: { type: String, required: true },
  editData: { type: Object, default: null },
})
const emit = defineEmits(['update:open', 'saved'])
const store = useInterviewStore()

const modalOpen = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v),
})

const mode = ref('edit')
const tagVisible = ref(false)
const tagInput = ref('')
const tagInputRef = ref(null)

// Algo Dedicated State
const algoAnalysis = ref('')
const algoCode = ref('') // Default blank
const codeLang = ref('java')
const algoMode = ref('leetcode') // 'leetcode' | 'acm'

// Dynamic CodeMirror 6 Language & Theme Extensions
const cmExtensions = computed(() => {
  const exts = [oneDark]
  const l = codeLang.value
  if (l === 'java') exts.push(java())
  else if (l === 'cpp') exts.push(cpp())
  else if (l === 'python') exts.push(python())
  else if (l === 'javascript' || l === 'typescript') exts.push(javascript({ typescript: l === 'typescript' }))
  else if (l === 'rust') exts.push(rust())
  else if (l === 'sql') exts.push(sql())
  else exts.push(java())
  return exts
})

const baguPresets = ['操作系统', '计算机网络', 'JVM', 'MySQL', 'Redis', 'Spring Boot', '并发编程', '微服务', 'MQ消息队列', '设计模式']
const algoPresets = ['双指针', '动态规划', '二叉树', '链表', '二分查找', '滑动窗口', '回溯算法', '贪心算法', '深度优先DFS', '广度优先BFS', '哈希表', '单调栈']

const currentPresets = computed(() => {
  return form.value.type === '八股文' ? baguPresets : algoPresets
})

// Templates Library: LeetCode Mode vs ACM Mode
const templates = {
  leetcode: {
    java: `class Solution {
    public int solve(int[] nums, int target) {
        // 在此编写 LeetCode 核心解题逻辑
        
        return 0;
    }
}`,
    cpp: `class Solution {
public:
    int solve(vector<int>& nums, int target) {
        // 在此编写 LeetCode 核心解题逻辑
        
        return 0;
    }
};`,
    python: `class Solution:
    def solve(self, nums: List[int], target: int) -> int:
        # 在此编写 LeetCode 核心解题逻辑
        
        return 0`,
    go: `func solve(nums []int, target int) int {
    // 在此编写 LeetCode 核心解题逻辑
    
    return 0
}`,
    javascript: `/**
 * @param {number[]} nums
 * @param {number} target
 * @return {number}
 */
var solve = function(nums, target) {
    // 在此编写 LeetCode 核心解题逻辑
    
    return 0;
};`,
    typescript: `function solve(nums: number[], target: number): number {
    // 在此编写 LeetCode 核心解题逻辑
    
    return 0;
};`,
    rust: `impl Solution {
    pub fn solve(nums: Vec<i32>, target: i32) -> i32 {
        // 在此编写 LeetCode 核心解题逻辑
        0
    }
}`,
    sql: `SELECT 
    id, name 
FROM 
    Users 
WHERE 
    status = 1;`
  },
  acm: {
    java: `import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        // 读取多组测试用例标准输入
        while (sc.hasNext()) {
            int n = sc.nextInt();
            
            // 编写算法逻辑并输出至控制台
            System.out.println(n);
        }
    }
}`,
    cpp: `#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

int main() {
    // 优化标准输入输出速度
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    
    int n;
    while (cin >> n) {
        // 编写算法逻辑并输出
        cout << n << "\\n";
    }
    return 0;
}`,
    python: `import sys

def solve():
    # 从标准输入读取数据
    for line in sys.stdin:
        parts = line.strip().split()
        if not parts:
            continue
        
        # 编写算法逻辑并打印结果
        print(parts)

if __name__ == "__main__":
    solve()`,
    go: `package main

import (
    "bufio"
    "fmt"
    "os"
)

func main() {
    scanner := bufio.NewScanner(os.Stdin)
    for scanner.Scan() {
        line := scanner.Text()
        // 编写算法逻辑并输出
        fmt.Println(line)
    }
}`,
    javascript: `const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

rl.on('line', (line) => {
    // 编写 ACM 模式标准输入输出逻辑
    console.log(line);
});`,
    typescript: `import * as readline from 'readline';

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

rl.on('line', (line: string) => {
    console.log(line);
});`,
    rust: `use std::io::{self, BufRead};

fn main() {
    let stdin = io::stdin();
    for line in stdin.lock().lines() {
        let line = line.unwrap();
        // 编写算法逻辑
        println!("{}", line);
    }
}`,
    sql: `-- ACM 复杂查询模板
SELECT 
    department_id, COUNT(*) AS total
FROM 
    employees
GROUP BY 
    department_id
HAVING 
    total > 5;`
  }
}

const baguTpl = `## 核心结论速记
在这里用 2-3 句话总结该知识点的本质原理与核心答案...

## 详细机制深度剖析
1. **底层数据结构 / 协议**：
2. **工作执行流程**：

## 常见面试追问
- 追问：高并发场景下如何优化？
- 解答：...`

const defaultForm = () => ({
  type: '八股文',
  title: '',
  content: '',
  difficulty: '中等',
  status: '未掌握',
  tags: [],
})
const form = ref(defaultForm())

watch(() => props.open, (v) => {
  if (v) {
    if (props.editData) {
      form.value = { ...props.editData, tags: [...(props.editData.tags || [])] }
      if (form.value.type === '算法题') {
        const content = form.value.content || ''
        const codeMatch = content.match(/```(\w*)\n?([\s\S]*?)```/)
        if (codeMatch) {
          codeLang.value = codeMatch[1] || 'java'
          algoCode.value = codeMatch[2] || ''
          algoAnalysis.value = content.replace(/```(\w*)\n?([\s\S]*?)```/, '').trim()
          algoMode.value = algoCode.value.includes('public static void main') || algoCode.value.includes('cin >>') || algoCode.value.includes('sys.stdin') ? 'acm' : 'leetcode'
        } else {
          algoAnalysis.value = content
          algoCode.value = ''
        }
      }
    } else {
      form.value = defaultForm()
      algoAnalysis.value = ''
      algoCode.value = '' // Default is empty!
      codeLang.value = 'java'
      algoMode.value = 'leetcode'
    }
    mode.value = 'edit'
    tagVisible.value = false
  }
})

function switchType(type) {
  form.value.type = type
}

function insertTemplate() {
  const modeTpls = templates[algoMode.value] || templates.leetcode
  const tpl = modeTpls[codeLang.value] || modeTpls.java
  algoCode.value = tpl
  message.success(`已填入 ${algoMode.value.toUpperCase()} 模式下的 ${codeLang.value.toUpperCase()} 模板`)
}

function clearCode() {
  algoCode.value = ''
}

function removeTag(t) {
  form.value.tags = form.value.tags.filter(x => x !== t)
}

function showTagInput() {
  tagVisible.value = true
  nextTick(() => tagInputRef.value?.focus())
}

function confirmTag() {
  const v = tagInput.value.trim()
  if (v && !form.value.tags.includes(v)) {
    form.value.tags.push(v)
  }
  tagVisible.value = false
  tagInput.value = ''
}

function togglePreset(p) {
  const idx = form.value.tags.indexOf(p)
  if (idx === -1) form.value.tags.push(p)
  else form.value.tags.splice(idx, 1)
}

function insertSnippet(type) {
  if (type === 'code') {
    form.value.content = (form.value.content || '') + '\n```java\n// 编写代码\n```\n'
  } else if (type === 'h2') {
    form.value.content = (form.value.content || '') + '\n## 重点小结\n'
  } else if (type === 'list') {
    form.value.content = (form.value.content || '') + '\n- 关键要点 1\n- 关键要点 2\n'
  }
}

function handleSubmit() {
  if (form.value.type === '算法题') {
    const analysis = algoAnalysis.value.trim()
    const modeTag = algoMode.value === 'acm' ? '【ACM 模式】' : '【LeetCode 模式】'
    const codeBlock = algoCode.value ? `\`\`\`${codeLang.value}\n${algoCode.value}\n\`\`\`` : ''
    
    let full = ''
    if (analysis) full += analysis
    if (codeBlock) {
      full += `${full ? '\n\n' : ''}## 代码实现 (${modeTag})\n${codeBlock}`
    }
    form.value.content = full
  }

  if (props.editData) {
    store.updateEntry(props.editData.id, form.value)
    message.success('题目信息已成功更新 ✅')
  } else {
    store.addEntry({ ...form.value, companyId: props.companyId })
    message.success('题目已成功录入企业题库 🎉')
  }
  emit('saved')
  emit('update:open', false)
}

function renderBaguMd(content) {
  if (!content) return '<p style="color:var(--text-3); text-align:center; padding: 40px 0;">暂无内容，请切换到编辑模式输入内容</p>'
  return content
    .replace(/```(\w*)\n?([\s\S]*?)```/g, (_, lang, code) => {
      return `<pre class="render-code-box"><code class="hljs">${code}</code></pre>`
    })
    .replace(/^### (.+)$/gm, '<h3 class="render-h3">$1</h3>')
    .replace(/^## (.+)$/gm, '<h2 class="render-h2">$1</h2>')
    .replace(/^# (.+)$/gm, '<h1 class="render-h1">$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong class="render-strong">$1</strong>')
    .replace(/`(.+?)`/g, '<code class="render-inline-code">$1</code>')
    .replace(/^- (.+)$/gm, '<div class="render-li">• $1</div>')
    .replace(/\n/g, '<br>')
}
</script>

<style scoped>
.entry-modal-form {
  padding-top: 4px;
}

/* Content Type Grid */
.content-type-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.type-selection-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  border-radius: 12px;
  border: 1.5px solid var(--border);
  background: var(--bg-subtle);
  cursor: pointer;
  transition: all 0.2s ease;
}
.type-selection-card:hover {
  background: #ffffff;
  border-color: var(--border-hover);
}

.tsc-icon-circle {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.tsc-blue { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
.tsc-green { background: #ecfdf5; color: #059669; border: 1px solid #a7f3d0; }

.tsc-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 2px;
}
.tsc-desc {
  font-size: 11px;
  color: var(--text-3);
}

.card-bagu-active {
  background: #ffffff !important;
  border-color: var(--accent) !important;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.15), 0 4px 12px rgba(0, 0, 0, 0.05);
}
.card-algo-active {
  background: #ffffff !important;
  border-color: #059669 !important;
  box-shadow: 0 0 0 2px rgba(5, 150, 105, 0.15), 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* Dual Column */
.dual-row-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 16px;
}

.dual-col-item {
  display: flex;
  flex-direction: column;
}

.item-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
  margin-bottom: 8px;
}

.chips-container {
  display: flex;
  gap: 8px;
  width: 100%;
}

.selection-chip {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg-subtle);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-2);
  cursor: pointer;
  transition: all 0.18s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}
.selection-chip:hover {
  background: #ffffff;
  border-color: var(--accent-light);
}

.chip-selected.diff-简单 { background: #ecfdf5 !important; border-color: #059669 !important; color: #059669 !important; font-weight: 600; }
.chip-selected.diff-中等 { background: #fffbeb !important; border-color: #d97706 !important; color: #d97706 !important; font-weight: 600; }
.chip-selected.diff-困难 { background: #fff1f2 !important; border-color: #e11d48 !important; color: #e11d48 !important; font-weight: 600; }

.chip-selected.status-未掌握 { background: #fff1f2 !important; border-color: #e11d48 !important; color: #e11d48 !important; font-weight: 600; }
.chip-selected.status-学习中 { background: #fffbeb !important; border-color: #d97706 !important; color: #d97706 !important; font-weight: 600; }
.chip-selected.status-已掌握 { background: #ecfdf5 !important; border-color: #059669 !important; color: #059669 !important; font-weight: 600; }

/* Tag Management */
.tag-management-card {
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 12px 16px;
}

.current-tags-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  min-height: 30px;
  margin-bottom: 8px;
}

.active-tag-pill {
  border-radius: 6px !important;
}

.add-custom-tag-btn {
  font-size: 12px;
  color: var(--accent);
  background: #ffffff;
  border: 1px dashed var(--accent-border);
  padding: 2px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.18s;
}
.add-custom-tag-btn:hover {
  background: var(--accent-subtle);
  border-color: var(--accent);
}

.preset-tags-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  border-top: 1px solid var(--border);
  padding-top: 8px;
}

.ptr-title {
  font-size: 12px;
  color: var(--text-3);
}

.ptr-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.ptr-item {
  font-size: 11px;
  color: var(--text-2);
  padding: 2px 7px;
  border-radius: 6px;
  background: #ffffff;
  border: 1px solid var(--border);
  cursor: pointer;
  transition: all 0.18s;
}
.ptr-item:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.ptr-active {
  background: var(--accent-subtle) !important;
  color: var(--accent) !important;
  border-color: var(--accent-border) !important;
  font-weight: 600;
}

/* ===== Algo Dedicated Section ===== */
.algo-dedicated-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.algo-analysis-input {
  border-radius: 8px !important;
  font-size: 13px !important;
  line-height: 1.6 !important;
}

/* CodeMirror Editor Card */
.codemirror-editor-wrapper {
  background: #282c34;
  border-radius: 12px;
  border: 1px solid #1e2227;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.cme-header {
  height: 42px;
  background: #21252b;
  border-bottom: 1px solid #181a1f;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cme-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mac-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  display: inline-block;
}
.red-dot { background: #ff5f56; }
.yellow-dot { background: #ffbd2e; }
.green-dot { background: #27c93f; }

/* Mode Capsule (LeetCode vs ACM) */
.mode-capsule {
  display: flex;
  background: #181a1f;
  padding: 2px;
  border-radius: 6px;
  border: 1px solid #333842;
}

.mode-btn {
  background: transparent;
  border: none;
  color: #7f848e;
  font-size: 11.5px;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.18s;
}
.mode-btn.active {
  background: #4f46e5;
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.code-lang-selector {
  width: 105px;
}
:deep(.code-lang-selector .ant-select-selector) {
  background: #282c34 !important;
  border-color: #3e4451 !important;
  color: #abb2bf !important;
  height: 26px !important;
  padding: 0 8px !important;
  font-size: 12px !important;
  font-weight: 600 !important;
}

.cme-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tool-action-btn {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #abb2bf;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.18s;
}
.tool-action-btn:hover {
  background: rgba(79, 70, 229, 0.3);
  border-color: #4f46e5;
  color: #ffffff;
}

/* Mode Desc Bar */
.mode-desc-bar {
  background: #1e2227;
  padding: 6px 14px;
  font-size: 11.5px;
  color: #abb2bf;
  border-bottom: 1px solid #181a1f;
  line-height: 1.5;
}
.mode-desc-bar code {
  background: rgba(255, 255, 255, 0.1);
  color: #61afef;
  padding: 1px 4px;
  border-radius: 3px;
  font-family: 'JetBrains Mono', monospace;
}

/* CodeMirror In-place Live Syntax Highlighting Surface */
.editor-cm-container {
  background: #282c34;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13.5px;
  line-height: 1.6;
}

:deep(.cm-editor) {
  height: 300px !important;
  background: #282c34 !important;
}
:deep(.cm-scroller) {
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace !important;
  font-size: 13.5px !important;
  line-height: 1.65 !important;
}
:deep(.cm-gutters) {
  background: #21252b !important;
  color: #4b5263 !important;
  border-right: 1px solid #181a1f !important;
}
:deep(.cm-activeLineGutter) {
  background: #282c34 !important;
  color: #abb2bf !important;
}
:deep(.cm-activeLine) {
  background: rgba(255, 255, 255, 0.03) !important;
}
:deep(.cm-cursor) {
  border-left-color: #528bff !important;
  border-left-width: 2px !important;
}

/* ===== Markdown Editor (Bagu) ===== */
.markdown-editor-wrapper {
  border: 1px solid var(--border);
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
}

.editor-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: var(--bg-subtle);
  border-bottom: 1px solid var(--border);
}

.syntax-helpers {
  display: flex;
  gap: 6px;
}

.helper-tool-btn {
  font-size: 12px;
  color: var(--text-2);
  background: #ffffff;
  border: 1px solid var(--border);
  padding: 2px 8px;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  transition: all 0.18s;
}
.helper-tool-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.mode-tab-group {
  display: flex;
  background: #ffffff;
  padding: 2px;
  border-radius: 6px;
  border: 1px solid var(--border);
}

.mode-switch-btn {
  background: transparent;
  border: none;
  font-size: 12px;
  color: var(--text-3);
  padding: 3px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.18s;
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.mode-switch-btn.active {
  background: var(--accent-subtle);
  color: var(--accent);
  font-weight: 600;
}

.editor-textarea-field {
  border: none !important;
  border-radius: 0 !important;
  padding: 12px 16px !important;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-1) !important;
}

.editor-preview-field {
  min-height: 250px;
  padding: 16px 20px;
  color: var(--text-1);
  line-height: 1.85;
  font-size: 14px;
}

.modal-bottom-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.submit-btn {
  min-width: 120px;
}
</style>
