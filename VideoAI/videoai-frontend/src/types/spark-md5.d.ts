declare module 'spark-md5' {
  export default class SparkMD5 {
    static ArrayBuffer: new () => {
      append(buffer: ArrayBuffer): void
      end(raw?: boolean): string
      reset(): void
    }
  }
}
