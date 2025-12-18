import { getApiUrl, API_CONFIG } from '@/config/api'

export const loginApi = async (id, username, password) => {
  try {
    const response = await fetch(getApiUrl(API_CONFIG.ENDPOINTS.LOGIN), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id, username, password })
    });
    return await response.json();
  } catch (error) {
    throw error;
  }
};

export const registerApi = async (id, username, password) => {
  try {
    const response = await fetch(getApiUrl(API_CONFIG.ENDPOINTS.REGISTER), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id, username, password })
    });
    return await response.json();
  } catch (error) {
    throw error;
  }
};