export interface Authorization {
  username: string,
  permissions: string[]
}

export function validTokens():boolean {
  const data:AuthorizationFull|null=getTokenInformationFull();
  if(data===null) return false;

  return data.exp>Date.now()/1000;
}
export function getTokenInformation():Authorization|null {
  return getTokenInformationFull();
}
export function deleteTokens():void {
  localStorage.removeItem(accessTokenLocalstorageName);
  localStorage.removeItem(refreshTokenLocalstorageName);
}

export interface Tokens {
  access: string,
  refresh: string
}
const accessTokenLocalstorageName:string="accessToken";
const refreshTokenLocalstorageName:string="refreshToken";
export function getTokens():Tokens|null {
  if(
    localStorage.getItem(accessTokenLocalstorageName)!==null&&
    localStorage.getItem(refreshTokenLocalstorageName)!==null
  ) {
    return {
      access: localStorage.getItem(accessTokenLocalstorageName)??"", 
      refresh: localStorage.getItem(refreshTokenLocalstorageName)??""
    };
  } else {
    return null;
  }
}
function saveTokens(tokens: Tokens):void {
  localStorage.setItem(accessTokenLocalstorageName, tokens.access);
  localStorage.setItem(refreshTokenLocalstorageName, tokens.refresh);
}

/*{
  "sub": "8e4c0fae-1ad7-4aa6-ad2b-afd035a4c725",
  "iat": 1722847653,
  "exp": 1722848253,
  "username": "Haart",
  "permissions": [
    "addTextToGlobal",
    "viewText",
    "editText"
  ]
}*/
interface AuthorizationFull extends Authorization {
  iat: number,
  exp: number
}
function getTokenInformationFull():AuthorizationFull|null {
  const tokens:Tokens|null=getTokens();
  if(tokens===null) return null;

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
export function signIn(username:string, userPassword: string):Promise<boolean> {
  return fetch(
    `${process.env.NEXT_PUBLIC_API_USER_HOST}/registry/signIn`,
    {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({name: username, password: userPassword})
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
      saveTokens(res_);
      return true;
    }
  }).catch(e => e);
}
export function refreshTokens():Promise<boolean> {
  const {access: accessToken, refresh: refreshToken} = getTokens()??{access: "", refresh: ""};
  if(!accessToken||!refreshToken) return new Promise(() => {return false;});
  return ((accessToken:string, refreshToken:string) => fetch(
    `${process.env.NEXT_PUBLIC_API_USER_HOST}/registry/refreshToken`,
    {
      //cache: 'no-store',
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
    if(res===false) {
      deleteTokens();
      return false;
    } else {
      if(!res||!res.accessToken||!res.refreshToken) return false;
      const res_:Tokens = {access: res.accessToken, refresh: res.refreshToken};
      saveTokens(res_);
      return true;
    }
  }).catch(e => e))(accessToken, refreshToken);
}

export function changeUsername(
  currentPassword: string, 
  newUsername: string
):Promise<boolean> {
  const {access: accessToken} = getTokens()??{};
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
  currentPassword: string, 
  newPassword: string
):Promise<boolean> {
  const {access: accessToken} = getTokens()??{};
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
export function deleteUser(currentPassword: string):Promise<boolean> {
  const {access: accessToken} = getTokens()??{};
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
