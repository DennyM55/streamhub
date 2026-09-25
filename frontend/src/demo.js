// Explicit presentation mode: no email identity, network API, or shared account data.
export const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'
export const movies = [
  { id: 1, title: 'Elephants Dream', genre: 'Fantasy', releaseYear: 2006, durationMinutes: 11,
    description: 'The complete open film: two explorers navigate a strange mechanical world. © 2006 Blender Foundation / Netherlands Media Art Institute, CC BY 2.5.',
    mediaUrl: '/media/elephants-dream-full.mp4' },
  { id: 2, title: 'Big Buck Bunny', genre: 'Animation', releaseYear: 2008, durationMinutes: 10,
    description: 'The complete open film: a gentle giant rabbit stands up to three mischievous woodland creatures. © 2008 Blender Foundation, CC BY 3.0.',
    mediaUrl: '/media/big-buck-bunny-full.mp4' },
]
function fail(message, status) { throw Object.assign(new Error(message), { status }) }
const key = token => `streamhub-demo:${token}`
export async function demoApi(path, token, options = {}) {
  const url = new URL(path, 'https://demo.invalid')
  const method = options.method || 'GET'
  if (url.pathname.startsWith('/users')) fail('Email accounts are disabled. Use the guest demo.', 403)
  if (path === '/demo/session' && method === 'POST') {
    const token = `guest-${crypto.randomUUID()}`
    localStorage.setItem(key(token), JSON.stringify({ favorites: [], history: [] }))
    return { token }
  }
  if (path === '/movies/genres') return [...new Set(movies.map(m => m.genre))].sort()
  if (url.pathname === '/movies' && method === 'GET') {
    const search = (url.searchParams.get('search') || '').toLowerCase()
    const genre = url.searchParams.get('genre')
    const filtered = movies.filter(m => (!genre || m.genre === genre) && `${m.title} ${m.description}`.toLowerCase().includes(search))
    const size = Math.max(1, Number(url.searchParams.get('size')) || 12)
    const page = Math.max(0, Number(url.searchParams.get('page')) || 0)
    return { content: filtered.slice(page * size, (page + 1) * size), totalElements: filtered.length, totalPages: Math.ceil(filtered.length / size) }
  }
  const movieMatch = url.pathname.match(/^\/movies\/(\d+)$/)
  if (movieMatch && method === 'GET') return movies.find(m => m.id === Number(movieMatch[1])) || fail('Movie not found', 404)
  if (!token?.startsWith('guest-')) fail('Start a new guest demo.', 401)
  let saved
  try { saved = JSON.parse(localStorage.getItem(key(token))) } catch { fail('Start a new guest demo.', 401) }
  if (!saved || !Array.isArray(saved.favorites) || !Array.isArray(saved.history)) fail('Start a new guest demo.', 401)
  if (path === '/favorites' && method === 'GET') return saved.favorites
  if (path === '/history' && method === 'GET') return saved.history
  const match = url.pathname.match(/^\/(favorites|history)\/(\d+)$/)
  if (!match) fail('Demo action not found', 404)
  const movie = movies.find(m => m.id === Number(match[2]))
  if (!movie) fail('Movie not found', 404)
  let result = { id: movie.id, movieId: movie.id, movieTitle: movie.title }
  if (match[1] === 'favorites' && ['POST', 'DELETE'].includes(method)) {
    saved.favorites = saved.favorites.filter(f => f.movieId !== movie.id)
    if (method === 'POST') saved.favorites.push(result)
    else result = null
  } else if (match[1] === 'history' && method === 'PUT') {
    const { progressSeconds } = JSON.parse(options.body || '{}')
    if (!Number.isInteger(progressSeconds) || progressSeconds < 0 || progressSeconds > movie.durationMinutes * 60) fail('Invalid progress', 400)
    result = { ...result, progressSeconds, lastWatchedAt: new Date().toISOString() }
    saved.history = [result, ...saved.history.filter(h => h.movieId !== movie.id)]
  } else fail('Demo action not available', 405)
  localStorage.setItem(key(token), JSON.stringify(saved))
  return result
}
