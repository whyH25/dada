export const getTestMessage = async () => {
  const response = await fetch('http://localhost:8080/api/test')
  return await response.text()
}