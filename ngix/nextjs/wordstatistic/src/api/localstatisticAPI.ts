import { getTokens } from "./userAPI";
import { Word } from './globalstatisticAPI';

export interface Topic {
    name:string
}
export interface TextListEntity {
    name:string
}
export interface Text extends TextListEntity {
    topic:Topic,
    text:string
}

export function getAllTotics():Promise<Topic[]> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/topicsAndTexts/getAllTopicsForUser`,
        {
          method: 'get',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: null
        }
      ).then(res => res.json()).catch(e => e);
}
export function getAllTexts(topicName:string):Promise<TextListEntity[]> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/topicsAndTexts`+
            `/getAllTextsForTopic?topicName=${topicName}`,
        {
          method: 'get',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: null
        }
      ).then(res => res.json()).catch(e => e);
}
export function getText(topicName:string, textName:string):Promise<Text> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/topicsAndTexts/`+
            `getTextContent?topicName=${topicName}&textName=${textName}`,
        {
          method: 'get',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: null
        }
      ).then(res => res.json()).catch(e => e);
}

export function addTopic(topicName:string):Promise<boolean> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/topicsAndTexts/addNewTopic`,
        {
          method: 'post',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: JSON.stringify({name: topicName})
        }
      ).then(res => res.status==200).catch(e => e);
}
export function addText(topicName:string, textName:string, textContent: string):Promise<boolean> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/topicsAndTexts/addNewText`,
        {
          method: 'post',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: JSON.stringify({topic: topicName, name: textName, text: textContent})
        }
      ).then(res => res.status==200).catch(e => e);
}

export function updateTopic(oldTopicName:string, newTopicName:string):Promise<boolean> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/topicsAndTexts/updateTopic`,
        {
          method: 'put',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: JSON.stringify({oldName: oldTopicName, newName: newTopicName})
        }
      ).then(res => res.status==200).catch(e => e);
}
export function updateText(
    topicName:string, 
    oldTextName:string, 
    newTextName:string, 
    newContent:string|undefined
):Promise<boolean> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/topicsAndTexts/updateText`,
        {
          method: 'put',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: JSON.stringify({
            topic: topicName, 
            newName: newTextName, 
            oldName: oldTextName, 
            text: newContent
        })
        }
      ).then(res => res.status==200).catch(e => e);
}

export function deleteTopic(topicName:string):Promise<boolean> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/topicsAndTexts/deleteTopic`,
        {
          method: 'delete',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: JSON.stringify({name: topicName})
        }
      ).then(res => res.status==200).catch(e => e);
}
export function deleteText(topicName:string, textName:string):Promise<boolean> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/topicsAndTexts/deleteText`,
        {
          method: 'delete',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: JSON.stringify({topic: topicName, name: textName})
        }
      ).then(res => res.status==200).catch(e => e);
}

export function getMostPopularWordsForUser(limit:number):Promise<Word[]> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/localStatistic/`+
            `getMostPopularWordsForUser?limit=${limit}`,
        {
          method: 'get',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: null
        }
      ).then(res => res.status==200).catch(e => e);
}
export function getMostPopularWordsForTopic(topicName:string, limit:number):Promise<Word[]> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/localStatistic/`+
            `getMostPopularWordsForTopic?topicName=${topicName}&limit=${limit}`,
        {
          method: 'get',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: null
        }
      ).then(res => res.status==200).catch(e => e);
}
export function getMostPopularWordsForText(
    topicName:string, 
    textName:string, 
    limit:number
):Promise<Word[]> {
    const {access: accessToken}=getTokens()??{access: ""};
    return fetch(
        `${process.env.NEXT_PUBLIC_API_LOCALSTATISTIC_HOST}/localStatistic`+
            `/getMostPopularWordsForText?topicName=${topicName}&textName=${textName}&limit=${limit}`,
        {
          method: 'get',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          },
          body: null
        }
      ).then(res => res.status==200).catch(e => e);
}
