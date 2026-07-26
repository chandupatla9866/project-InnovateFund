import { describe, expect, it } from 'vitest'
import { cn } from './cn'

describe('cn', () => {
  it('joins truthy class names with a space', () => {
    expect(cn('a', 'b', 'c')).toBe('a b c')
  })

  it('drops falsy values (false, undefined, null, empty string)', () => {
    expect(cn('a', false, undefined, null, '', 'b')).toBe('a b')
  })

  it('returns an empty string when given nothing usable', () => {
    expect(cn(false, undefined, null)).toBe('')
  })

  it('supports the conditional-class pattern used throughout the app', () => {
    const isActive = true
    const isDisabled = false
    expect(cn('base', isActive && 'active', isDisabled && 'disabled')).toBe('base active')
  })
})
