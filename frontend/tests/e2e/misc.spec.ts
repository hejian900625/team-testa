import { expect, test } from './fixtures'

test('404 view renders for unknown routes', async ({ page }) => {
  await page.goto('/this-route-does-not-exist')
  await expect(page.locator('main')).toBeVisible()
  await expect(page.locator('body')).not.toHaveText('')
})

test('global header is present on every page', async ({ page }) => {
  await page.goto('/agents')
  await expect(page.getByTestId('app-brand')).toBeVisible()
  await expect(page.getByTestId('nav-agents')).toBeVisible()
})

test('header nav link stays visible across pages', async ({ page }) => {
  await page.goto('/agents')
  await expect(page.getByTestId('nav-agents')).toBeVisible()
  await page.goto('/agents/new')
  await expect(page.getByTestId('nav-agents')).toBeVisible()
  await page.goto('/agents')
  await expect(page.getByTestId('nav-agents')).toBeVisible()
})

test('app loads without console errors', async ({ page }) => {
  const logs: string[] = []
  page.on('console', (msg) => {
    if (msg.type() === 'error') logs.push(msg.text())
  })
  await page.goto('/agents')
  await expect(page.getByTestId('agent-list-page')).toBeVisible()
  expect(logs).toEqual([])
})
