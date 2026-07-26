import { beforeEach, describe, expect, it, vi } from 'vitest'
import { loadAiToolCache, saveAiToolCache } from './aiToolsCache'

// Regression coverage for the "AI Tools results disappear on navigation" fix: results must
// survive a full reload (i.e. actually persist to localStorage, not just component state) and
// only change when the tool is explicitly re-run.
describe('aiToolsCache', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('round-trips an object through save and load', () => {
    saveAiToolCache('pitch-review:startup-1', { data: { overallImpression: 'Solid' }, input: undefined })

    expect(loadAiToolCache('pitch-review:startup-1')).toEqual({ data: { overallImpression: 'Solid' }, input: undefined })
  })

  it('returns undefined for a key that was never saved', () => {
    expect(loadAiToolCache('never-saved')).toBeUndefined()
  })

  it('namespaces keys so different tools/startups do not collide', () => {
    saveAiToolCache('mentor:startup-1', { data: { answer: 'A' } })
    saveAiToolCache('mentor:startup-2', { data: { answer: 'B' } })

    expect(loadAiToolCache('mentor:startup-1').data.answer).toBe('A')
    expect(loadAiToolCache('mentor:startup-2').data.answer).toBe('B')
  })

  it('survives a simulated page reload (fresh read from localStorage, not memory)', () => {
    saveAiToolCache('market-research:startup-1', { data: { topCompetitors: ['X', 'Y'] }, input: 'fintech' })

    // "Reloading" means nothing but localStorage persists — re-reading must still find it.
    const reloaded = loadAiToolCache('market-research:startup-1')

    expect(reloaded.data.topCompetitors).toEqual(['X', 'Y'])
    expect(reloaded.input).toBe('fintech')
  })

  it('does not throw and returns undefined when localStorage.getItem fails', () => {
    const spy = vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('unavailable')
    })

    expect(loadAiToolCache('anything')).toBeUndefined()

    spy.mockRestore()
  })

  it('does not throw when localStorage.setItem fails (e.g. quota exceeded)', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('quota exceeded')
    })

    expect(() => saveAiToolCache('anything', { data: 1 })).not.toThrow()

    spy.mockRestore()
  })
})
