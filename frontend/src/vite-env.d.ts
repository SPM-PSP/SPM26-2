/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_AUTH_MOCK?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
