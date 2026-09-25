import { beforeEach, describe, expect, it, vi } from 'vitest'
import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import App from './App'

const film = { id: 1, title: 'Big Buck Bunny', description: 'A story from the forest.', genre: 'Animation', releaseYear: 2008, durationMinutes: 10, thumbnailUrl: null, mediaUrl: null }
const savedFilm = { ...film, id: 99, title: 'Saved on another page', genre: 'Fantasy' }
const watchedFilm = { ...film, id: 55, title: 'Previously watched film' }
let favorites, history, requests, catalogueUnavailable

beforeEach(() => {
  favorites = []; history = []; requests = []; catalogueUnavailable = false
  vi.stubGlobal('fetch', vi.fn(async (url, options = {}) => {
    const route = new URL(url)
    requests.push({ path: route.pathname, query: route.searchParams, ...options })
    let data
    if (route.pathname === '/movies/genres') data = ['Animation', 'Fantasy', 'Sci-Fi']
    else if (route.pathname === '/movies') {
      if (catalogueUnavailable) throw new Error('offline')
      data = { content: [film], totalPages: 3, totalElements: 25 }
    }
    else if (route.pathname === '/movies/99') data = savedFilm
    else if (route.pathname === '/movies/55') data = watchedFilm
    else if (route.pathname === '/demo/session') data = { token: 'test-demo-token' }
    else if (route.pathname === '/users') data = { id: 42, name: 'Viewer', email: 'viewer@example.com' }
    else if (route.pathname === '/users/login') data = { token: 'test-account-token' }
    else if (route.pathname === '/favorites' && !options.method) data = favorites
    else if (route.pathname === '/history' && !options.method) data = history
    else if (route.pathname === '/favorites/1' && options.method === 'POST') data = { id: 5, userId: 2, movieId: 1, movieTitle: film.title }
    else if (route.pathname === '/favorites/1' && options.method === 'DELETE') return new Response(null, { status: 204 })
    else if (route.pathname === '/history/1' && options.method === 'PUT') data = { id: 6, userId: 2, movieId: 1, movieTitle: film.title, progressSeconds: JSON.parse(options.body).progressSeconds }
    else throw new Error(`Unexpected test request: ${options.method || 'GET'} ${route.pathname}`)
    return new Response(JSON.stringify(data), { status: 200, headers: { 'Content-Type': 'application/json' } })
  }))
})

describe('StreamHub frontend API flows', () => {
  it('loads the real API catalogue and sends search, genre and pagination filters', async () => {
    render(<App />)
    await screen.findByRole('button', { name: 'Open Big Buck Bunny' })
    expect(screen.getByText('25 TITLES')).toBeTruthy()
    fireEvent.change(screen.getByLabelText('Search movies'), { target: { value: 'rabbit' } })
    await waitFor(() => expect(requests.some(r => r.query?.get('search') === 'rabbit')).toBe(true))
    fireEvent.change(screen.getByLabelText('Filter by genre'), { target: { value: 'Fantasy' } })
    await waitFor(() => expect(requests.some(r => r.query?.get('genre') === 'Fantasy')).toBe(true))
    fireEvent.click(screen.getByRole('button', { name: 'Next →' }))
    await waitFor(() => expect(requests.some(r => r.query?.get('page') === '1')).toBe(true))
  })

  it('resolves saved favourites and history outside the current catalogue page', async () => {
    favorites = [{ id: 10, movieId: 99, movieTitle: savedFilm.title }]
    history = [{ id: 11, movieId: 55, movieTitle: watchedFilm.title, progressSeconds: 120 }]
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Try the demo →' }))
    await screen.findByRole('button', { name: 'Sign out' })
    fireEvent.click(screen.getByRole('button', { name: 'Favourites', exact: true }))
    await screen.findByRole('button', { name: 'Open Saved on another page' })
    expect(screen.queryByRole('button', { name: 'Open Big Buck Bunny' })).toBeNull()
    fireEvent.click(screen.getByRole('button', { name: 'Continue watching', exact: true }))
    await screen.findByRole('button', { name: 'Open Previously watched film' })
    expect(screen.getByText('10 min · 2 min watched')).toBeTruthy()
  })

  it('adds/removes favourites and saves actual progress with authorization', async () => {
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Try the demo →' }))
    await screen.findByRole('button', { name: 'Sign out' })
    const add = await screen.findByRole('button', { name: 'Add Big Buck Bunny to favourites' })
    fireEvent.click(add)
    await screen.findByRole('button', { name: 'Remove Big Buck Bunny from favourites' })
    fireEvent.click(screen.getByRole('button', { name: 'Open Big Buck Bunny' }))
    const detail = screen.getByRole('dialog', { name: 'Big Buck Bunny' })
    fireEvent.change(within(detail).getByLabelText(/Watch progress/), { target: { value: '90' } })
    fireEvent.click(within(detail).getByRole('button', { name: 'Save progress' }))
    await waitFor(() => expect(requests.find(r => r.path === '/history/1')?.body).toBe('{"progressSeconds":90}'))
    expect(requests.find(r => r.path === '/history/1').headers.Authorization).toBe('Bearer test-demo-token')
    await screen.findByText('Watch progress saved to your account.')
    fireEvent.click(within(detail).getByRole('button', { name: 'Close dialog' }))
    fireEvent.click(screen.getByRole('button', { name: 'Remove Big Buck Bunny from favourites' }))
    await screen.findByRole('button', { name: 'Add Big Buck Bunny to favourites' })
    fireEvent.click(screen.getByRole('button', { name: 'Continue watching', exact: true }))
    await screen.findByRole('button', { name: 'Open Big Buck Bunny' })
    expect(screen.getByText('10 min · 1 min watched')).toBeTruthy()
  })

  it('registers, signs in and clears the session on sign out', async () => {
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Sign in →' }))
    fireEvent.click(screen.getByRole('button', { name: 'Create an account' }))
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'Viewer' } })
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'viewer@example.com' } })
    fireEvent.change(screen.getByLabelText(/Password/), { target: { value: 'test-password-123' } })
    fireEvent.click(screen.getByRole('button', { name: 'Create account →' }))
    await screen.findByRole('button', { name: 'Sign out' })
    expect(requests.find(r => r.path === '/users').body).toContain('viewer@example.com')
    expect(sessionStorage.getItem('streamhub-token')).toBe('test-account-token')
    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }))
    expect(sessionStorage.getItem('streamhub-token')).toBeNull()
    expect(screen.getByRole('button', { name: 'Sign in →' })).toBeTruthy()
  })

  it('shows connection failure honestly and recovers through retry', async () => {
    catalogueUnavailable = true
    render(<App />)
    await screen.findByRole('alert')
    expect(screen.getByText('Let\'s reconnect.')).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Open Big Buck Bunny' })).toBeNull()
    catalogueUnavailable = false
    fireEvent.click(screen.getByRole('button', { name: 'Try again' }))
    await screen.findByRole('button', { name: 'Open Big Buck Bunny' })
    expect(screen.queryByRole('alert')).toBeNull()
  })

  it('explains prolonged startup and offers retry when a cold-start request times out', async () => {
    vi.useFakeTimers()
    vi.stubGlobal('fetch', vi.fn((_url, options) => new Promise((_resolve, reject) => {
      options.signal.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')))
    })))
    try {
      render(<App />)
      await act(async () => { await vi.advanceTimersByTimeAsync(6_001) })
      expect(screen.getByText(/The demo services may be waking up/)).toBeTruthy()
      expect(screen.queryByRole('alert')).toBeNull()
      await act(async () => { await vi.advanceTimersByTimeAsync(300_000) })
      expect(screen.getByRole('alert').textContent).toContain('taking longer than expected')
      expect(screen.getByRole('button', { name: 'Try again' })).toBeTruthy()
    } finally { vi.useRealTimers() }
  })


  it('ignores a favourite response that arrives after sign out', async () => {
    const originalFetch = fetch
    let finishFavorite
    vi.stubGlobal('fetch', vi.fn((url, options) => {
      if (new URL(url).pathname === '/favorites/1' && options.method === 'POST') {
        return new Promise(resolve => { finishFavorite = resolve })
      }
      return originalFetch(url, options)
    }))
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Try the demo →' }))
    await screen.findByRole('button', { name: 'Sign out' })
    fireEvent.click(await screen.findByRole('button', { name: 'Add Big Buck Bunny to favourites' }))
    await waitFor(() => expect(finishFavorite).toBeTypeOf('function'))
    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }))
    await act(async () => { finishFavorite(new Response(JSON.stringify({ id: 10, movieId: 1 }))) })
    expect(screen.queryByRole('button', { name: 'Remove Big Buck Bunny from favourites' })).toBeNull()
    expect(screen.getByRole('button', { name: 'Add Big Buck Bunny to favourites' })).toBeTruthy()
    expect(sessionStorage.getItem('streamhub-token')).toBeNull()
  })

  it('keeps a deleted title removable without breaking the saved collection', async () => {
    favorites = [{ id: 10, movieId: 99, movieTitle: savedFilm.title }]
    const originalFetch = fetch
    vi.stubGlobal('fetch', vi.fn((url, options) => {
      if (new URL(url).pathname === '/movies/99') return Promise.resolve(new Response('{"message":"Movie not found"}', { status: 404 }))
      if (new URL(url).pathname === '/favorites/99' && options.method === 'DELETE') return Promise.resolve(new Response(null, { status: 204 }))
      return originalFetch(url, options)
    }))
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Try the demo →' }))
    await screen.findByRole('button', { name: 'Sign out' })
    fireEvent.click(screen.getByRole('button', { name: 'Favourites', exact: true }))
    await screen.findByRole('button', { name: 'Open Saved on another page' })
    fireEvent.click(screen.getByRole('button', { name: 'Remove Saved on another page from favourites' }))
    await screen.findByText('Make room for your favourites.')
    expect(screen.queryByRole('alert')).toBeNull()
  })

  it('resumes saved video progress when account history arrives after metadata', async () => {
    const playableFilm = { ...film, mediaUrl: 'https://example.com/film.mp4' }
    const originalFetch = fetch
    let finishHistory
    vi.stubGlobal('fetch', vi.fn((url, options) => {
      const path = new URL(url).pathname
      if (path === '/movies') return Promise.resolve(new Response(JSON.stringify({ content: [playableFilm], totalPages: 1, totalElements: 1 })))
      if (path === '/movies/1') return Promise.resolve(new Response(JSON.stringify(playableFilm)))
      if (path === '/history') return new Promise(resolve => { finishHistory = resolve })
      return originalFetch(url, options)
    }))
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Try the demo →' }))
    await screen.findByRole('button', { name: 'Sign out' })
    fireEvent.click(await screen.findByRole('button', { name: 'Open Big Buck Bunny' }))
    const detail = screen.getByRole('dialog', { name: 'Big Buck Bunny' })
    const player = detail.querySelector('video')
    Object.defineProperty(player, 'duration', { value: 600, configurable: true })
    fireEvent.loadedMetadata(player)
    expect(player.currentTime).toBe(0)
    await act(async () => { finishHistory(new Response(JSON.stringify([{ id: 11, movieId: 1, movieTitle: film.title, progressSeconds: 90 }]))) })
    await waitFor(() => expect(player.currentTime).toBe(90))
    expect(within(detail).getByLabelText(/Watch progress/).value).toBe('90')
  })

  it('returns to sign in when an authenticated mutation receives an expired session', async () => {
    const originalFetch = fetch
    vi.stubGlobal('fetch', vi.fn((url, options) => {
      if (new URL(url).pathname === '/favorites/1') return Promise.resolve(new Response('{"message":"Unauthorized"}', { status: 401 }))
      return originalFetch(url, options)
    }))
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Try the demo →' }))
    await screen.findByRole('button', { name: 'Sign out' })
    fireEvent.click(await screen.findByRole('button', { name: 'Add Big Buck Bunny to favourites' }))
    await screen.findByRole('button', { name: 'Sign in →' })
    expect(screen.getByText('Your session has expired. Please sign in again.')).toBeTruthy()
    expect(sessionStorage.getItem('streamhub-token')).toBeNull()
  })

})
