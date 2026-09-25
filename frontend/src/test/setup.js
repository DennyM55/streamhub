import { afterEach } from 'vitest'
import { cleanup } from '@testing-library/react'

HTMLDialogElement.prototype.showModal = function () { this.setAttribute('open', '') }
HTMLDialogElement.prototype.close = function () { this.removeAttribute('open') }
afterEach(() => { cleanup(); sessionStorage.clear() })
