import Cookies from 'js-cookie'

const TokenKey = 'X-Litemall-Admin-Token'

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token, { 
    secure: window.location.protocol === 'https:',
    sameSite: 'strict'
  })
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}
