export interface Tokens {
  access: string,
  refresh: string
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

export function signIn(userName:string, userPassword: string):Promise<Tokens|false> {
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
export function refreshTokens(accessToken:string, refreshToken: string):Promise<Tokens|false> {
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
      if(!res||!res.access||!res.refresh) return false;
      const res_:Tokens = {access: res.access, refresh: res.refresh};
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


