#!/usr/bin/env python3
"""Exercise the real HTTP API. Only creates disposable demo data; never logs tokens."""
import json
import os
import sys
import uuid
from urllib.error import HTTPError
from urllib.request import Request, urlopen

BASE = os.environ.get('API_BASE_URL', 'http://localhost:8080').rstrip('/')
ADMIN = os.environ.get('ADMIN_API_KEY', '')
TIMEOUT = float(os.environ.get('API_TIMEOUT_SECONDS', '20'))
checks = []

def call(path, method='GET', body=None, token=None, admin=False, expected=200):
    headers = {}
    if body is not None:
        headers['Content-Type'] = 'application/json'
    if token:
        headers['Authorization'] = 'Bearer ' + token
    if admin:
        headers['X-Admin-Key'] = ADMIN
    request = Request(BASE + path, data=json.dumps(body).encode() if body is not None else None,
                      method=method, headers=headers)
    try:
        with urlopen(request, timeout=TIMEOUT) as response:
            code, raw = response.status, response.read()
    except HTTPError as error:
        code, raw = error.code, error.read()
    expected = (expected,) if isinstance(expected, int) else expected
    assert code in expected, f'{method} {path}: expected {expected}, got {code}: {raw[:250]!r}'
    return json.loads(raw) if raw else None

def passed(name):
    checks.append(name)
    print('PASS ' + name, flush=True)

try:
    assert call('/health')['status'] == 'UP'
    passed('API health')
    catalogue = call('/movies?size=2&page=0')
    assert 0 < len(catalogue['content']) <= 2
    movie = catalogue['content'][0]
    assert call('/movies/' + str(movie['id']))['title'] == movie['title']
    passed('Catalogue read and pagination')
    from urllib.parse import urlencode
    filtered = call('/movies?' + urlencode({'search': movie['title'], 'genre': movie['genre']}))
    assert any(m['id'] == movie['id'] for m in filtered['content'])
    passed('Catalogue search and genre filtering')
    call('/movies/999999999', expected=404)
    call('/favorites', expected=401)
    call('/favorites', token='invalid-token', expected=401)
    passed('Missing resource and JWT rejection')
    a = call('/demo/session', 'POST')['token']
    b = call('/demo/session', 'POST')['token']
    assert a != b
    assert call('/favorites', token=a) == []
    call('/favorites/' + str(movie['id']), 'POST', token=a)
    call('/favorites/' + str(movie['id']), 'POST', token=a, expected=(200, 400, 409))
    assert len(call('/favorites', token=a)) == 1
    assert call('/favorites', token=b) == []
    passed('Isolated guest sessions and idempotent favourites')
    call('/history/' + str(movie['id']), 'PUT', {'progressSeconds': -1}, a, expected=400)
    saved = call('/history/' + str(movie['id']), 'PUT', {'progressSeconds': 12}, a)
    assert saved['progressSeconds'] == 12
    assert call('/history', token=a)[0]['movieId'] == movie['id']
    assert call('/history', token=b) == []
    passed('Progress validation, persistence, and user isolation')
    call('/favorites/' + str(movie['id']), 'DELETE', token=a, expected=204)
    assert call('/favorites', token=a) == []
    passed('Favourite removal')
    email = 'smoke-' + uuid.uuid4().hex + '@example.invalid'
    password = uuid.uuid4().hex
    call('/users', 'POST', {'name': 'API smoke test', 'email': email, 'password': password}, expected=(200, 201))
    login = call('/users/login', 'POST', {'email': email, 'password': password})
    assert login['token']
    call('/users/login', 'POST', {'email': email, 'password': 'wrong-password'}, expected=401)
    passed('Registration, login, password rejection')
    body = {'title': 'Smoke ' + uuid.uuid4().hex, 'description': 'Disposable smoke-test record',
            'genre': 'Test', 'releaseYear': 2026, 'durationMinutes': 2,
            'thumbnailUrl': None, 'mediaUrl': None}
    call('/movies', 'POST', body, a, expected=(401, 403))
    passed('Public users cannot mutate the catalogue')
    if ADMIN:
        created = call('/movies', 'POST', body, admin=True, expected=(200, 201))
        body['title'] += ' updated'
        updated = call('/movies/' + str(created['id']), 'PUT', body, admin=True)
        assert updated['title'] == body['title']
        assert call('/movies/' + str(created['id']))['title'] == body['title']
        call('/movies/' + str(created['id']), 'DELETE', admin=True, expected=204)
        call('/movies/' + str(created['id']), expected=404)
        passed('Administrator CRUD and cache invalidation')
    else:
        print('SKIP admin CRUD: ADMIN_API_KEY not supplied')
    print(f'COMPLETE: {len(checks)} real API checks passed')
except Exception as error:
    print(f'FAIL after {len(checks)} checks: {error}', file=sys.stderr)
    sys.exit(1)
