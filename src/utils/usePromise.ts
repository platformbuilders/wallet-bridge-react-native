import { useState } from 'react';

export const usePromise = (promise: (...params: any[]) => Promise<any>) => {
  const [loading, setLoading] = useState(false);

  const callPromise = async (...params: any[]): Promise<any> => {
    try {
      setLoading(true);
      return await promise(...params);
    } catch (error) {
      throw error;
    } finally {
      setLoading(false);
    }
  };

  return { loading, callPromise } as const;
};
