const CN_TO_API: Record<string, string> = {
  简单: 'easy',
  中等: 'medium',
  困难: 'hard',
}

const API_TO_CN: Record<string, string> = {
  easy: '简单',
  medium: '中等',
  hard: '困难',
}

export function difficultyToApi(label: string): string {
  return CN_TO_API[label] ?? label
}

export function difficultyFromApi(code: string): string {
  return API_TO_CN[code] ?? code
}
