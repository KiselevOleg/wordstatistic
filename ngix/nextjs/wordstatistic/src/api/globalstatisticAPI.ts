import { getTokens } from "./userAPI";

export interface Word {
  name: string,
  count: number
}
export function getMostPopularWords(limit: number):Promise<Word[]> {
  return fetch(
    `${process.env.NEXT_PUBLIC_API_GLOBALSTATISTIC_HOST}/globalStatistic/getMostPopularWords?limit=${limit}`,
    {
      method: 'get',
      headers: { 'Content-Type': 'application/json' },
      body: null
    }
  ).then(res => res.json()).catch(e => e);
}

export function addText(text: string):Promise<boolean> {
  const {access: accessToken}=getTokens()??{access: ""};
  if (!accessToken) return new Promise(() => {return false;});
  return fetch(
    `${process.env.NEXT_PUBLIC_API_GLOBALSTATISTIC_HOST}/globalStatistic/addText`,
    {
      method: 'post',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`
      },
      body: text
    }
  ).then(res => res.status==200).catch(e => e);
}
