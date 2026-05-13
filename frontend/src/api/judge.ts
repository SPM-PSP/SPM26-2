import http from './http'
import type { JudgeResponse } from '@/types/api'

export type JudgeLanguageKey = 'cpp' | 'java' | 'python'

export interface JudgeSubmitBody {
  code: string
  input: string
  answer: string
  language: JudgeLanguageKey
  timeLimit?: number
  memoryLimit?: string
}

export async function judgeSubmit(body: JudgeSubmitBody) {
  // #region agent log
  fetch('http://127.0.0.1:7701/ingest/53fbfa53-e7fd-4c8a-9ae7-3df73473f0c6', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-Debug-Session-Id': '69ddfc' },
    body: JSON.stringify({
      sessionId: '69ddfc',
      location: 'judge.ts:judgeSubmit',
      message: 'submit request',
      data: { language: body.language, codeLen: body.code?.length ?? 0 },
      timestamp: Date.now(),
      hypothesisId: 'H1',
    }),
  }).catch(() => {})
  // #endregion
  const { data } = await http.post<JudgeResponse>('/api/judge/submit', body)
  // #region agent log
  fetch('http://127.0.0.1:7701/ingest/53fbfa53-e7fd-4c8a-9ae7-3df73473f0c6', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-Debug-Session-Id': '69ddfc' },
    body: JSON.stringify({
      sessionId: '69ddfc',
      location: 'judge.ts:judgeSubmit',
      message: 'submit response',
      data: { verdictCode: data.code, status: data.status },
      timestamp: Date.now(),
      hypothesisId: 'H1',
    }),
  }).catch(() => {})
  // #endregion
  return data
}
