interface Tokens {
  access: string,
  refresh: string
}
const accessTokenLocalstorageName:string="accessToken";
const refreshTokenLocalstorageName:string="refreshToken";
function saveTokens(tokens: Tokens):void {
  localStorage.setItem(accessTokenLocalstorageName, tokens.access);
  localStorage.setItem(refreshTokenLocalstorageName, tokens.refresh);
}
export function deleteTokens():void {
  localStorage.removeItem(accessTokenLocalstorageName);
  localStorage.removeItem(refreshTokenLocalstorageName);
}
export function validTokens():Promise<boolean> {
  const accessToken:string = localStorage.getItem(accessTokenLocalstorageName)??"";
  const refreshToken:string = localStorage.getItem(refreshTokenLocalstorageName)??"";
  return refreshTokens(accessToken, refreshToken).then(res => {
    if(res===false) {
      return false;
    }
    saveTokens(res);
    return true;
  });
}
function getTokens():Tokens|null {
  const accessToken:string|null=localStorage.getItem(accessTokenLocalstorageName);
  const refreshToken:string|null=localStorage.getItem(refreshTokenLocalstorageName);
  if(accessToken===null||refreshToken===null) return null;
  return {access: accessToken, refresh: refreshToken};
}
export interface Authorization {
  username: string,
  permissions: string[]
}
export function getTokenInformation():Authorization|null {
  const tokens:Tokens|null=getTokens();
  if (tokens===null) return null;
  const base64Url:string = tokens.access.split('.')[1];
  const base64:string = base64Url.replace('-', '+').replace('_', '/');
  return JSON.parse(window.atob(base64));
}

export function signUp(userName:string, userPassword: string):Promise<boolean> {
  return fetch(
    `${process.env.NEXT_PUBLIC_API_USER_HOST}/registry/signUp`,
    {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({name: userName, password: userPassword})
    }
  ).then(res => res.status==200).catch(e => e);
}
export function signIn(userName:string, userPassword: string):Promise<boolean> {
  return fetch(
    `${process.env.NEXT_PUBLIC_API_USER_HOST}/registry/signIn`,
    {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({name: userName, password: userPassword})
    }
  ).then(res => {
    if(res.status==200) {
      return res.json();
    } else return false;
  }).then(res => {
    if(res===false) return false;
    else {
      if(!res||!res.access||!res.refresh) return false;
      const res_:Tokens = {access: res.access, refresh: res.refresh};
      return res_;
    }
  }).catch(e => e);
}
function refreshTokens(accessToken:string, refreshToken: string):Promise<Tokens|false> {
  return fetch(
    `${process.env.NEXT_PUBLIC_API_USER_HOST}/registry/refreshToken`,
    {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        accessToken: accessToken, refreshToken: refreshToken
      })
    }
  ).then(res => {
    if(res.status==200) {
      return res.json();
    } else return false;
  }).then(res => {
    if(res===false) return false;
    else {
      if(!res||!res.accessToken||!res.refreshToken) return false;
      const res_:Tokens = {access: res.accessToken, refresh: res.refreshToken};
      return res_;
    }
  }).catch(e => e);
}

export function changeUsername(
  accessToken: string, 
  currentPassword: string, 
  newUsername: string
):Promise<boolean> {
  return fetch(
    `${process.env.NEXT_PUBLIC_API_USER_HOST}/changeUser/changeUsername`,
    {
      method: 'put',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`
      },
      body: JSON.stringify({currentPassword: currentPassword, newUsername: newUsername})
    }
  ).then(res => res.status==200).catch(e => e);
}
export function changePassword(
  accessToken: string, 
  currentPassword: string, 
  newPassword: string
):Promise<boolean> {
  return fetch(
    `${process.env.NEXT_PUBLIC_API_USER_HOST}/changeUser/changePassword`,
    {
      method: 'put',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`
      },
      body: JSON.stringify({currentPassword: currentPassword, newPassword: newPassword})
    }
  ).then(res => res.status==200).catch(e => e);
}
export function deleteUser(accessToken: string, currentPassword: string):Promise<boolean> {
  return fetch(
    `${process.env.NEXT_PUBLIC_API_USER_HOST}/changeUser/deleteUser`,
    {
      method: 'delete',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`
      },
      body: JSON.stringify({currentPassword: currentPassword})
    }
  ).then(res => res.status==200).catch(e => e);
}


