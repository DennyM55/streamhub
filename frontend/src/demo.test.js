import { beforeEach, expect, test } from 'vitest'
import { demoApi, movies } from './demo.js'
beforeEach(() => localStorage.clear())
test('guest collection persists and stays separate from another guest', async () => {
  const first = await demoApi('/demo/session', null, { method: 'POST' })
  const second = await demoApi('/demo/session', null, { method: 'POST' })
  await demoApi('/favorites/1', first.token, { method: 'POST' })
  await demoApi('/history/1', first.token, { method: 'PUT', body: JSON.stringify({ progressSeconds: 300 }) })
  expect(await demoApi('/favorites', first.token)).toHaveLength(1)
  expect((await demoApi('/history', first.token))[0].progressSeconds).toBe(300)
  expect(await demoApi('/favorites', second.token)).toEqual([])
  expect(await demoApi('/history', second.token)).toEqual([])
  await demoApi('/favorites/1', first.token, { method: 'DELETE' })
  expect(await demoApi('/favorites', first.token)).toEqual([])
})
test('email credentials cannot create or enter a demo session', async () => {
  for (const path of ['/users', '/users/login']) {
    await expect(demoApi(path, null, { method: 'POST', body: '{"email":"someone@example.com","password":"anything"}' })).rejects.toMatchObject({ status: 403 })
  }
  await expect(demoApi('/favorites', 'arbitrary-token')).rejects.toMatchObject({ status: 401 })
})
test('search and genres match complete local film records', async () => {
  expect((await demoApi('/movies?search=bunny')).content.map(m => m.title)).toEqual(['Big Buck Bunny'])
  expect((await demoApi('/movies?genre=Fantasy')).totalElements).toBe(1)
  expect(movies.every(m => m.mediaUrl.endsWith('-full.mp4') && m.durationMinutes >= 10)).toBe(true)
})
