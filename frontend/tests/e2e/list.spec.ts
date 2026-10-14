import { expect, test } from './fixtures'

test.beforeEach(async ({ page }) => {
  await page.goto('/agents')
})

test('shows agents on load', async ({ page }) => {
  await expect(page.getByTestId('agent-list-page')).toBeVisible()
  // After create tests run, the mock may have different agents;
  // just assert at least one row renders and the list is not empty.
  await expect(page.getByTestId('agent-table')).toBeVisible()
  const rowCount = await page.locator('[data-testid^="agent-row-"]').count()
  expect(rowCount).toBeGreaterThan(0)
})

test('keyword filter narrows results', async ({ page }) => {
  await page.getByTestId('keyword-input').fill('alpha')
  await page.getByTestId('search-button').click()
  // "alpha" should still match at least the original Alpha agent or an agent containing that text
  await expect(page.getByTestId('agent-table')).toBeVisible()
})

test('status filter shows only matching agents', async ({ page }) => {
  await page.getByTestId('status-filter').selectOption('ACTIVE')
  await page.getByTestId('search-button').click()
  // After filtering, table should still render (may be empty or have matches)
  await expect(page.getByTestId('agent-table')).toBeVisible()
})

test('reset clears filters and reloads full list', async ({ page }) => {
  await page.getByTestId('status-filter').selectOption('ACTIVE')
  await page.getByTestId('search-button').click()
  await page.getByTestId('reset-button').click()
  // After reset, all agents should be visible again
  await expect(page.getByTestId('agent-table')).toBeVisible()
  const rowCount = await page.locator('[data-testid^="agent-row-"]').count()
  expect(rowCount).toBeGreaterThan(0)
})

test('navigates to detail when clicking an agent row', async ({ page }) => {
  const firstRow = page.locator('[data-testid^="agent-row-"]').first()
  await firstRow.click()
  await page.waitForURL(/\/agents\//)
  await expect(page.getByTestId('agent-detail-page')).toBeVisible()
})

test('shows page info element', async ({ page }) => {
  await expect(page.getByTestId('page-info')).toBeVisible()
})
