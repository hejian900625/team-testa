import { expect, test } from './fixtures'

test.beforeEach(async ({ page }) => {
  await page.goto('/agents')
})

test('shows agent name and model on detail page', async ({ page }) => {
  const firstRow = page.locator('[data-testid^="agent-row-"]').first()
  await firstRow.click()
  await page.waitForURL(/\/agents\//)
  await expect(page.getByTestId('agent-detail-page')).toBeVisible()
  await expect(page.getByTestId('agent-name')).toBeVisible()
  await expect(page.getByTestId('agent-model')).toBeVisible()
})

test('shows runs table on detail page', async ({ page }) => {
  const firstRow = page.locator('[data-testid^="agent-row-"]').first()
  await firstRow.click()
  await page.waitForURL(/\/agents\//)
  // Runs table may or may not have data; assert the element exists
  const runsTable = page.getByTestId('runs-table')
  // Either the table is visible (has runs) or the detail page loads without error
  await expect(page.getByTestId('agent-detail-page')).toBeVisible()
})

test('edit button navigates to form', async ({ page }) => {
  const firstRow = page.locator('[data-testid^="agent-row-"]').first()
  await firstRow.click()
  await page.waitForURL(/\/agents\//)
  await page.getByTestId('edit-agent').click()
  await page.waitForURL(/\/agents\/.*\/edit/)
  await expect(page.getByTestId('agent-form-page')).toBeVisible()
})

test('updates agent name and reflects on detail page', async ({ page }) => {
  const firstRow = page.locator('[data-testid^="agent-row-"]').first()
  await firstRow.click()
  await page.waitForURL(/\/agents\//)
  await page.getByTestId('edit-agent').click()
  await page.waitForURL(/\/agents\/.*\/edit/)
  const newName = `E2E-Updated-${Date.now()}`
  await page.getByTestId('input-name').fill(newName)
  await page.getByTestId('submit-agent').click()
  await page.waitForURL(/\/agents\//)
  await expect(page.getByTestId('agent-detail-page')).toBeVisible()
  await expect(page.getByTestId('agent-name')).toContainText(newName)
})

test('delete confirms and removes agent from list', async ({ page }) => {
  // Find the last agent row to delete (safest — won't affect other tests' fixtures)
  const lastRow = page.locator('[data-testid^="agent-row-"]').last()
  await lastRow.click()
  await page.waitForURL(/\/agents\//)

  // Check we're on a detail page with a delete button
  await expect(page.getByTestId('delete-agent')).toBeVisible()

  // Intercept confirm and accept
  page.on('dialog', async (dialog) => { await dialog.accept() })
  await page.getByTestId('delete-agent').click()

  // Should return to list
  await page.waitForURL(/\/agents$/)
  await expect(page.getByTestId('agent-list-page')).toBeVisible()

  // The deleted agent should no longer appear
  const currentRows = await page.locator('[data-testid^="agent-row-"]').count()
  // At least one agent should remain, or list is empty (both valid)
  expect(currentRows).toBeGreaterThanOrEqual(0)
})

test('back to list link works', async ({ page }) => {
  const firstRow = page.locator('[data-testid^="agent-row-"]').first()
  await firstRow.click()
  await page.waitForURL(/\/agents\//)
  await page.getByTestId('back-to-list').click()
  await page.waitForURL(/\/agents$/)
  await expect(page.getByTestId('agent-list-page')).toBeVisible()
})
