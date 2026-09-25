// Bundle complete public, CC-licensed films on the same static origin as the app.
// A failed/truncated download fails the build instead of shipping a broken player.
import { mkdir, stat, rename, rm } from 'node:fs/promises'
import { createWriteStream } from 'node:fs'
import { Readable } from 'node:stream'
import { pipeline } from 'node:stream/promises'
if (process.env.VITE_DEMO_MODE === 'true') {
  await mkdir('public/media', { recursive: true })
  const sources = [
    ['elephants-dream-full.mp4', 'https://d2zihajmogu5jn.cloudfront.net/elephantsdream/ed_hd.mp4'],
    ['big-buck-bunny-full.mp4', 'https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4.zip'],
  ]
  for (const [name, source] of sources) {
    const target = `public/media/${name}`
    if ((await stat(target).catch(() => null))?.size > 10_000_000) continue
    console.log(`Downloading complete film: ${name}`)
    const response = await fetch(source, { signal: AbortSignal.timeout(180_000) })
    if (response.status !== 200) throw new Error(`Film download failed: ${name} (${response.status})`)
    try {
      await pipeline(Readable.fromWeb(response.body), createWriteStream(`${target}.part`))
      const size = (await stat(`${target}.part`)).size
      const expected = Number(response.headers.get('content-length'))
      if (size < 10_000_000 || (expected && size !== expected)) throw new Error(`Incomplete film: ${name}`)
      if (source.endsWith('.zip')) {
        // Extract only the known film, never arbitrary paths from an archive.
        const output = createWriteStream(`${target}.extracted`)
        const { spawn } = await import('node:child_process')
        const unzip = spawn('unzip', ['-p', `${target}.part`, 'BigBuckBunny_320x180.mp4'])
        const completed = new Promise((resolve, reject) => {
          unzip.once('error', reject)
          unzip.once('close', code => code === 0 ? resolve() : reject(new Error(`unzip failed: ${code}`)))
        })
        await Promise.all([pipeline(unzip.stdout, output), completed])
        await rename(`${target}.extracted`, target)
      } else await rename(`${target}.part`, target)
    } finally { await rm(`${target}.part`, { force: true }) }
  }
}
