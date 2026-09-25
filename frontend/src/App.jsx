import { useEffect, useRef, useState } from 'react'
import './App.css'

const API = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

async function api(path, token, options = {}) {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), 300_000)
  try {
    let response
    try {
      response = await fetch(`${API}${path}`, {
        ...options,
        signal: controller.signal,
        headers: {
          ...(options.body ? { 'Content-Type': 'application/json' } : {}),
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
      })
    } catch {
      throw new Error(controller.signal.aborted
        ? 'StreamHub is taking longer than expected to respond. The service may still be starting. Please retry.'
        : 'We could not reach StreamHub. Please try again shortly.')
    }
    if (response.status === 204) return null
    const data = await response.json().catch(() => {
      if (response.ok) throw new Error(controller.signal.aborted
        ? 'StreamHub is taking longer than expected to respond. Please retry.'
        : 'StreamHub returned an unexpected response. Please retry.')
      return null
    })
    if (!response.ok) {
      const error = new Error(data?.message || data?.detail || `The request failed (${response.status}). Please try again.`)
      error.status = response.status
      throw error
    }
    return data
  } finally { clearTimeout(timeout) }
}

function StartupNotice() {
  const [slow, setSlow] = useState(false)
  useEffect(() => {
    const timer = setTimeout(() => setSlow(true), 6_000)
    return () => clearTimeout(timer)
  }, [])
  if (!slow) return null
  return <p className="startup-notice" role="status"><span aria-hidden="true">◌</span> The demo services may be waking up. On free hosting, the first visit can take several minutes. We're still trying to connect.</p>
}

function safeMediaUrl(value) {
  if (!value) return null
  try {
    const url = new URL(value, window.location.origin)
    return ['http:', 'https:'].includes(url.protocol) ? url.href : null
  } catch { return null }
}

function Cover({ movie, index = 0 }) {
  const [failedUrl, setFailedUrl] = useState(null)
  const palettes = ['#37596e', '#72564d', '#5c5178', '#336862', '#83593c']
  const thumbnail = safeMediaUrl(movie?.thumbnailUrl)
  return <div className="cover" style={{ '--cover': palettes[Math.abs(movie?.id ?? index) % palettes.length] }}>
    {thumbnail && thumbnail !== failedUrl
      ? <img src={thumbnail} alt="" loading="lazy" onError={() => setFailedUrl(thumbnail)} />
      : <div className="poster-art"><span className="poster-orbit" /><span className="poster-star">✦</span><span className="poster-type">{movie?.title || 'A world of stories.'}</span><span className="poster-mark">THE STREAMHUB COLLECTION</span></div>}
  </div>
}

function MoviePlayer({ movie, resumeSeconds, historyLoading, onProgress }) {
  const video = useRef(null)
  const resumed = useRef(false)
  const [ready, setReady] = useState(false)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    if (!ready || historyLoading || resumed.current) return
    const player = video.current
    if (!player) return
    resumed.current = true
    const position = Math.min(resumeSeconds || 0, Number.isFinite(player.duration) ? player.duration : 0)
    player.currentTime = position
    onProgress(Math.floor(position))
  }, [ready, historyLoading, resumeSeconds, onProgress])

  return <div className="video-section"><video ref={video} controls preload="metadata" playsInline
    src={safeMediaUrl(movie.mediaUrl)} onLoadedMetadata={() => setReady(true)}
    onTimeUpdate={e => { if (resumed.current) onProgress(Math.floor(e.currentTarget.currentTime)) }}
    onError={() => setFailed(true)} />
    {failed ? <p className="error" role="status">The media source is unavailable right now. You can still save this title and track progress.</p>
      : <small>Playback updates the counter below. Save to keep your place.</small>}
  </div>
}

function Modal({ children, label, onClose, className = '' }) {
  const dialog = useRef(null)
  useEffect(() => {
    const element = dialog.current
    element.showModal()
    const oldOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => { element.close(); document.body.style.overflow = oldOverflow }
  }, [])
  return <dialog ref={dialog} className="dialog-shell" aria-label={label} onCancel={onClose} onClick={e => { if (e.target === e.currentTarget) onClose() }}>
    <div className={`modal ${className}`}><button className="close" onClick={onClose} aria-label="Close dialog">×</button>{children}</div>
  </dialog>
}

function Brand() {
  return <span className="logo"><span className="logo-icon">S<sup>✦</sup></span>STREAM<span>HUB</span></span>
}

export default function App() {
  const [token, setToken] = useState(() => sessionStorage.getItem('streamhub-token') || '')
  const [movies, setMovies] = useState([])
  const [favs, setFavs] = useState([])
  const [history, setHistory] = useState([])
  const [savedMovies, setSavedMovies] = useState({})
  const [search, setSearch] = useState('')
  const [genre, setGenre] = useState('')
  const [knownGenres, setKnownGenres] = useState([])
  const [tab, setTab] = useState('Discover')
  const [selected, setSelected] = useState(null)
  const [auth, setAuth] = useState(false)
  const [register, setRegister] = useState(false)
  const [fields, setFields] = useState({ name: '', email: '', password: '' })
  const [progress, setProgress] = useState(0)
  const [catalogueError, setCatalogueError] = useState('')
  const [savedError, setSavedError] = useState('')
  const [authError, setAuthError] = useState('')
  const [detailError, setDetailError] = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [savedLoading, setSavedLoading] = useState(Boolean(token))
  const [authBusy, setAuthBusy] = useState(false)
  const [progressBusy, setProgressBusy] = useState(false)
  const [favoriteBusy, setFavoriteBusy] = useState(new Set())
  const [page, setPage] = useState(0)
  const [pages, setPages] = useState(1)
  const [total, setTotal] = useState(0)
  const [catalogueTotal, setCatalogueTotal] = useState(null)
  const [retry, setRetry] = useState(0)
  const [savedRetry, setSavedRetry] = useState(0)
  const sessionEpoch = useRef(0)

  useEffect(() => {
    let active = true
    api('/movies/genres').then(data => { if (active && Array.isArray(data)) setKnownGenres(data) }).catch(() => {})
    return () => { active = false }
  }, [retry])

  useEffect(() => {
    let active = true
    const query = new URLSearchParams({ page: String(page), size: '12' })
    if (search.trim()) query.set('search', search.trim())
    if (genre) query.set('genre', genre)
    const timer = setTimeout(() => {
      setLoading(true); setCatalogueError('')
      api(`/movies?${query}`).then(data => {
      if (!active) return
      setMovies(data?.content || [])
      setPages(Math.max(data?.totalPages || 0, 1))
      setTotal(data?.totalElements || 0)
      if (!search.trim() && !genre) setCatalogueTotal(data?.totalElements || 0)
      setKnownGenres(old => [...new Set([...old, ...(data?.content || []).map(m => m.genre).filter(Boolean)])].sort())
      setCatalogueError('')
    }).catch(e => { if (active) setCatalogueError(e.message) }).finally(() => { if (active) setLoading(false) })
    }, 250)
    return () => { active = false; clearTimeout(timer) }
  }, [search, genre, page, retry])

  useEffect(() => {
    let active = true
    if (!token) return
    Promise.resolve().then(() => {
      if (!active) return []
      setSavedLoading(true); setSavedError('')
      return Promise.all([api('/favorites', token), api('/history', token)])
    }).then(async ([f, h]) => {
      if (!active) return
      const ids = [...new Set([...(f || []), ...(h || [])].map(item => item.movieId))]
      const records = Object.fromEntries([...(f || []), ...(h || [])].map(item => [item.movieId, item]))
      const entries = await Promise.all(ids.map(async id => {
        try { return [id, await api(`/movies/${id}`)] }
        catch (error) {
          if (error.status !== 404) throw error
          return [id, { id, title: records[id]?.movieTitle || 'Unavailable title', unavailable: true,
            description: 'This title is no longer available in the catalogue. You can still remove it from your favourites.' }]
        }
      }))
      if (!active) return
      setFavs(f || [])
      setHistory(h || [])
      setSavedMovies(Object.fromEntries(entries))
    }).catch(e => {
      if (!active) return
      if (e.status === 401) {
        sessionEpoch.current += 1
        sessionStorage.removeItem('streamhub-token')
        setToken(''); setFavs([]); setHistory([]); setSavedMovies({}); setTab('Discover'); setMessage('Your session has expired. Please sign in again.')
      } else setSavedError(e.message)
    }).finally(() => { if (active) setSavedLoading(false) })
    return () => { active = false }
  }, [token, savedRetry])

  useEffect(() => {
    if (!message) return
    const timer = setTimeout(() => setMessage(''), 5500)
    return () => clearTimeout(timer)
  }, [message])

  const favoriteIds = new Set(favs.map(f => f.movieId))
  const historyById = Object.fromEntries(history.map(h => [h.movieId, h]))
  const savedList = (tab === 'Favorites' ? favs : history).map(item => savedMovies[item.movieId]).filter(Boolean)
  const visible = tab === 'Discover' ? movies : savedList.filter(m => (!genre || m.genre === genre) && (!search.trim() || `${m.title} ${m.description || ''}`.toLowerCase().includes(search.trim().toLowerCase())))
  const isLoading = tab === 'Discover' ? loading : savedLoading
  const currentError = tab === 'Discover' ? catalogueError : savedError
  const genres = [...new Set([...knownGenres, ...Object.values(savedMovies).map(m => m.genre).filter(Boolean)])].sort()

  function establishSession(result) {
    if (!result?.token) throw new Error('Sign in did not return a session. Please try again.')
    sessionEpoch.current += 1
    sessionStorage.setItem('streamhub-token', result.token)
    setFavoriteBusy(new Set()); setProgressBusy(false); setSavedLoading(true); setToken(result.token); setAuth(false); setFields({ name: '', email: '', password: '' }); setAuthError('')
  }
  async function signIn(e) {
    e.preventDefault(); if (authBusy) return; setAuthError(''); setAuthBusy(true)
    try {
      if (register) await api('/users', null, { method: 'POST', body: JSON.stringify({ ...fields, name: fields.name.trim(), email: fields.email.trim() }) })
      const result = await api('/users/login', null, { method: 'POST', body: JSON.stringify({ email: fields.email.trim(), password: fields.password }) })
      establishSession(result); setMessage(register ? 'Your account is ready. Make yourself at home.' : 'Welcome back. Your collection is ready.')
    } catch (err) { setAuthError(err.message) } finally { setAuthBusy(false) }
  }
  async function startDemo() {
    if (authBusy) return
    setAuthError(''); setAuthBusy(true)
    try {
      establishSession(await api('/demo/session', null, { method: 'POST' }))
      setMessage('Your personal demo session is ready. Save a favourite to try it out.')
    } catch (err) { setAuthError(err.message); setAuth(true) } finally { setAuthBusy(false) }
  }
  function signOut() {
    sessionEpoch.current += 1
    setFavoriteBusy(new Set()); setProgressBusy(false)
    sessionStorage.removeItem('streamhub-token'); setToken(''); setFavs([]); setHistory([]); setSavedMovies({}); setSavedError(''); setSavedLoading(false); setTab('Discover'); setSelected(null); setSearch(''); setGenre(''); setPage(0); setMessage('You are signed out.')
  }
  function requireSignIn() { setAuthError(''); setAuth(true) }
  async function toggleFavorite(movie) {
    if (!token) { requireSignIn(); return }
    if (favoriteBusy.has(movie.id) || savedLoading || savedError) return
    const requestSession = sessionEpoch.current
    setFavoriteBusy(old => new Set([...old, movie.id]))
    try {
      if (favoriteIds.has(movie.id)) {
        await api(`/favorites/${movie.id}`, token, { method: 'DELETE' })
        if (sessionEpoch.current !== requestSession) return
        setFavs(old => old.filter(f => f.movieId !== movie.id)); setMessage('Removed from your favourites.')
      } else {
        const added = await api(`/favorites/${movie.id}`, token, { method: 'POST' })
        if (sessionEpoch.current !== requestSession) return
        setFavs(old => [...old.filter(f => f.movieId !== movie.id), added])
        setSavedMovies(old => ({ ...old, [movie.id]: movie })); setMessage('Added to your favourites.')
      }
    } catch (err) {
      if (sessionEpoch.current === requestSession) {
        if (err.status === 401) { signOut(); setMessage('Your session has expired. Please sign in again.') }
        else setMessage(err.message)
      }
    } finally {
      if (sessionEpoch.current === requestSession) setFavoriteBusy(old => { const next = new Set(old); next.delete(movie.id); return next })
    }
  }
  async function saveProgress(e) {
    e.preventDefault()
    if (!token) { requireSignIn(); return }
    if (progressBusy || savedLoading || savedError || selected?.unavailable) return
    const requestSession = sessionEpoch.current
    setProgressBusy(true); setDetailError('')
    try {
      const result = await api(`/history/${selected.id}`, token, { method: 'PUT', body: JSON.stringify({ progressSeconds: Number(progress) }) })
      if (sessionEpoch.current !== requestSession) return
      setHistory(old => [result, ...old.filter(h => h.movieId !== selected.id)])
      setSavedMovies(old => ({ ...old, [selected.id]: selected })); setMessage('Watch progress saved to your account.')
    } catch (err) {
      if (sessionEpoch.current === requestSession) {
        if (err.status === 401) { signOut(); setMessage('Your session has expired. Please sign in again.') }
        else setDetailError(err.message)
      }
    } finally { if (sessionEpoch.current === requestSession) setProgressBusy(false) }
  }
  function openMovie(movie) {
    setSelected(movie); setDetailError(''); setProgress(historyById[movie.id]?.progressSeconds || 0)
  }
  function switchTab(next) {
    if (next !== 'Discover' && !token) { requireSignIn(); return }
    setTab(next); setSearch(''); setGenre(''); setPage(0)
  }
  const metadata = movie => [movie.genre, movie.releaseYear, movie.durationMinutes && `${movie.durationMinutes} min`].filter(Boolean).join(' · ')

  return <div className="site">
    <a className="skip-link" href="#catalogue">Skip to catalogue</a>
    <header className="topbar"><a href="/" aria-label="StreamHub home"><Brand /></a><nav aria-label="Main navigation">{['Discover', 'Favorites', 'Continue watching'].map(n => <button key={n} className={tab === n ? 'active' : ''} aria-current={tab === n ? 'page' : undefined} onClick={() => switchTab(n)}>{n === 'Favorites' ? 'Favourites' : n}</button>)}</nav><div className="account"><span className={`live ${catalogueError ? 'offline' : ''}`}><i />{catalogueError ? 'CONNECTING' : loading ? 'LOADING' : 'LIVE CATALOGUE'}</span><button onClick={token ? signOut : requireSignIn}>{token ? 'Sign out' : 'Sign in →'}</button></div></header>

    {tab === 'Discover' && <section className="hero"><div className="hero-copy"><span className="kicker">— THE STORIES START HERE</span><h1>Your next<br /><em>great watch</em><br />starts now.</h1><p>A home for stories worth discovering. Explore the collection, keep your favourites close, and pick up where you left off.</p><div className="hero-links"><button className="primary" onClick={() => document.querySelector('#catalogue')?.scrollIntoView({ behavior: 'smooth' })}><span aria-hidden="true">▶ &nbsp;</span> Explore catalogue</button>{!token && <button className="outline" onClick={startDemo} disabled={authBusy}>{authBusy ? 'Starting…' : 'Try the demo →'}</button>}</div>{authBusy && !auth && <StartupNotice />}<div className="stats"><div><strong>{catalogueTotal === null ? '—' : String(catalogueTotal).padStart(2, '0')}</strong><small>TITLES TO DISCOVER</small></div><div><strong>Yours.</strong><small>A PERSONAL COLLECTION</small></div><div><strong>Anytime.</strong><small>PICK UP WHERE YOU LEFT OFF</small></div></div></div><div className="hero-visual" aria-hidden="true"><div className="ring" /><div className="hero-card behind"><Cover movie={movies[1]} index={1} /></div><div className="hero-card ahead"><Cover movie={movies[0]} index={0} /><div className="feature-caption"><small>FEATURED IN STREAMHUB</small><strong>{movies[0]?.title || 'Discover your story'}</strong></div></div><span className="visual-chip">✦ &nbsp; YOUR COLLECTION AWAITS</span></div></section>}

    <main id="catalogue" className={tab === 'Discover' ? '' : 'collection-page'}><div className="heading"><div><span className="kicker">THE COLLECTION / {tab === 'Favorites' ? 'FAVOURITES' : tab.toUpperCase()}</span><h2>{tab === 'Discover' ? 'Explore the catalogue' : tab === 'Favorites' ? 'Your favourites' : 'Continue watching'}</h2><p>{tab === 'Discover' ? 'A little something for every kind of movie night.' : tab === 'Favorites' ? 'Stories you want to keep close.' : 'Your watch history, ready when you are.'}</p></div><small>{tab === 'Discover' ? total : visible.length} {((tab === 'Discover' ? total : visible.length) === 1) ? 'TITLE' : 'TITLES'}</small></div><div className="filters"><label><span aria-hidden="true">⌕</span><input aria-label="Search movies" type="search" placeholder="Search for a story…" value={search} onChange={e => { setSearch(e.target.value); setPage(0) }} /></label><select aria-label="Filter by genre" value={genre} onChange={e => { setGenre(e.target.value); setPage(0) }}><option value="">All genres</option>{genres.map(g => <option key={g}>{g}</option>)}</select>{(search || genre) && <button className="clear-filters" onClick={() => { setSearch(''); setGenre(''); setPage(0) }}>Clear filters</button>}</div>
      {tab === 'Discover' && savedError && <div className="collection-warning" role="alert"><p>Your saved collection could not be loaded. {savedError}</p><button className="outline" onClick={() => setSavedRetry(r => r + 1)}>Reload collection</button></div>}
      {currentError ? <div className="empty error-state" role="alert"><span className="empty-symbol">↻</span><h3>Let's reconnect.</h3><p>{currentError}</p><button className="outline" onClick={() => tab === 'Discover' ? setRetry(r => r + 1) : setSavedRetry(r => r + 1)}>Try again</button></div> : isLoading ? <><StartupNotice /><div className="grid skeletons" role="status" aria-label="Loading movies">{Array.from({ length: 8 }, (_, i) => <div className="skeleton-card" key={i}><div /><span /><span /></div>)}</div></> : visible.length ? <div className="grid">{visible.map((movie, index) => <article className="card" key={movie.id}><button className="movie-open" onClick={() => openMovie(movie)} aria-label={`Open ${movie.title}`}><Cover movie={movie} index={index} /><span className="card-play" aria-hidden="true">▶</span></button><button className={`heart ${favoriteIds.has(movie.id) ? 'saved' : ''}`} disabled={favoriteBusy.has(movie.id) || savedLoading || Boolean(savedError)} onClick={() => toggleFavorite(movie)} aria-label={`${favoriteIds.has(movie.id) ? 'Remove' : 'Add'} ${movie.title} ${favoriteIds.has(movie.id) ? 'from' : 'to'} favourites`} aria-pressed={favoriteIds.has(movie.id)}>♥</button><span className="genre">{movie.genre || 'FEATURE'}{movie.releaseYear ? ` · ${movie.releaseYear}` : ''}</span><h3><button onClick={() => openMovie(movie)}>{movie.title}</button></h3><p>{movie.durationMinutes ? `${movie.durationMinutes} min` : 'Discover now'}{historyById[movie.id] ? ` · ${Math.floor(historyById[movie.id].progressSeconds / 60)} min watched` : ''}</p>{historyById[movie.id] && movie.durationMinutes > 0 && <div className="watch-meter" aria-label={`${Math.round(Math.min(100, historyById[movie.id].progressSeconds / (movie.durationMinutes * 60) * 100))}% watched`}><span style={{ width: `${Math.min(100, historyById[movie.id].progressSeconds / (movie.durationMinutes * 60) * 100)}%` }} /></div>}</article>)}</div> : <div className="empty"><strong>✦</strong><h3>{search || genre ? 'No matching titles' : tab === 'Discover' ? 'The collection is on its way' : tab === 'Favorites' ? 'Make room for your favourites.' : 'Your next watch starts here.'}</h3><p>{search || genre ? 'Try another search or clear the filters.' : tab === 'Discover' ? 'Check back soon for new stories.' : 'Explore a title and save it to build your collection.'}</p>{tab !== 'Discover' && <button className="outline" onClick={() => switchTab('Discover')}>Explore catalogue →</button>}</div>}
      {tab === 'Discover' && pages > 1 && !currentError && <div className="pagination"><button disabled={page === 0 || loading} onClick={() => setPage(p => p - 1)}>← Previous</button><span>Page {page + 1} of {pages}</span><button disabled={page >= pages - 1 || loading} onClick={() => setPage(p => p + 1)}>Next →</button></div>}
    </main>
    <footer><Brand /><p>Built for the love of stories.<br /><span>A personal project by Denny Mathew.</span></p><a href="https://github.com/DennyM55/streamhub" target="_blank" rel="noreferrer">EXPLORE THE PROJECT ↗</a></footer>
    {message && <div className="toast" role="status">{message}<button onClick={() => setMessage('')} aria-label="Dismiss notification">×</button></div>}

    {selected && <Modal label={selected.title} onClose={() => setSelected(null)} className="detail"><Cover movie={selected} /><div className="detail-body"><span className="kicker">NOW DISCOVERING</span><h2>{selected.title}</h2><div className="meta">{metadata(selected)}</div><p>{selected.description || 'A story worth discovering.'}</p><button className="outline" disabled={favoriteBusy.has(selected.id) || savedLoading || Boolean(savedError)} onClick={() => toggleFavorite(selected)}>{favoriteIds.has(selected.id) ? '♥ Saved to favourites' : '♡ Add to favourites'}</button>{safeMediaUrl(selected.mediaUrl) && <MoviePlayer key={`${selected.id}-${token}`} movie={selected} resumeSeconds={historyById[selected.id]?.progressSeconds || 0} historyLoading={savedLoading} onProgress={setProgress} />}{!safeMediaUrl(selected.mediaUrl) && <p className="preview-note">A video preview isn't available for this title. Save it to your collection or track your watch progress below.</p>}{safeMediaUrl(selected.mediaUrl) && ['Big Buck Bunny', 'Sintel', 'Tears of Steel', 'Elephants Dream'].includes(selected.title) && <p className="media-credit">{selected.title === 'Elephants Dream' ? '© 2006 Blender Foundation / Netherlands Media Art Institute · CC BY 2.5' : '© Blender Foundation · CC BY 3.0'} · <a href={selected.title === 'Elephants Dream' ? 'https://orange.blender.org/press/' : selected.title === 'Sintel' ? 'https://durian.blender.org/about/' : selected.title === 'Tears of Steel' ? 'https://mango.blender.org/about/' : 'https://peach.blender.org/about/'} target="_blank" rel="noreferrer">Film credits ↗</a></p>}{savedError && <div className="collection-warning" role="alert"><p>Your saved collection could not be loaded.</p><button className="outline" onClick={() => setSavedRetry(r => r + 1)}>Reload collection</button></div>}<form className="progress" onSubmit={saveProgress}><label htmlFor="progress-seconds">Watch progress <small>seconds watched</small></label><div><input id="progress-seconds" type="number" min="0" step="1" max={selected.durationMinutes ? selected.durationMinutes * 60 : undefined} required value={progress} onChange={e => setProgress(e.target.value)} /><button type="submit" className="primary" disabled={progressBusy || savedLoading || Boolean(savedError) || selected.unavailable}>{progressBusy ? 'Saving…' : token ? 'Save progress' : 'Sign in to save'}</button></div></form>{progressBusy && <StartupNotice />}{detailError && <div className="error" role="alert">{detailError}</div>}</div></Modal>}

    {auth && <Modal label={register ? 'Create an account' : 'Sign in'} onClose={() => setAuth(false)} className="auth"><span className="kicker">WELCOME TO STREAMHUB</span><h2>{register ? 'Start your collection.' : 'Welcome back.'}</h2><p>Save your favourites. Remember your place. Make movie night yours.</p><form onSubmit={signIn}>{register && <label>Name<input autoComplete="name" required maxLength="100" value={fields.name} onChange={e => setFields({ ...fields, name: e.target.value })} /></label>}<label>Email<input type="email" autoComplete="email" required value={fields.email} onChange={e => setFields({ ...fields, email: e.target.value })} /></label><label>Password<input type="password" autoComplete={register ? 'new-password' : 'current-password'} minLength="8" required value={fields.password} onChange={e => setFields({ ...fields, password: e.target.value })} />{register && <small className="input-hint">At least 8 characters</small>}</label>{authError && <div className="error" role="alert">{authError}</div>}<button className="primary" type="submit" disabled={authBusy}>{authBusy ? 'Please wait…' : register ? 'Create account →' : 'Sign in →'}</button></form>{authBusy && <StartupNotice />}<p className="switch">{register ? 'Already registered?' : 'New here?'} <button onClick={() => { setAuthError(''); setRegister(!register) }}>{register ? 'Sign in' : 'Create an account'}</button></p><div className="demo-option"><span>JUST EXPLORING?</span><button className="outline" disabled={authBusy} onClick={startDemo}>Try a personal demo session →</button><small>No email needed. Your demo collection is separate from everyone else's.</small></div></Modal>}
  </div>
}
