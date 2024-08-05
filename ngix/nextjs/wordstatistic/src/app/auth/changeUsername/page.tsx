'use client'

import React from "react";
import Link from "next/link";
import Styles from "./page.module.css";

import {changeUsername, validTokens, refreshTokens} from "../../../api/userAPI";

export default class Page extends React.Component<unknown, unknown> {
  constructor(props: unknown) {
    super(props);

    this.state = {
      
    };
  }

  componentDidMount(): void {
    if(!validTokens()) window.location.replace("/auth/signIn");
  }

  render():React.ReactNode {
    return <div className={Styles.mainContent}>
      <Form />
      <ChangePage />
    </div>;
  }
}

interface StateFormType {
  errorMessage?:string
}
class Form extends React.Component<unknown, StateFormType> {
  constructor(props: unknown) {
    super(props);

    this.state = {

    };
  }

  newUsernameInputRef:React.RefObject<HTMLInputElement> = React.createRef<HTMLInputElement>();
  currentPasswordInputRef:React.RefObject<HTMLInputElement> = React.createRef<HTMLInputElement>();

  actionButtonClickHandle = ():void => {
    if (!this.newUsernameInputRef.current?.checkValidity()) {
      this.setState({
        errorMessage: 
          "username is invalid (length from 1 to 30 is required and allowed only A-Z, a-z, _, 0-9)"
      });
      return;
    }
    if (!this.currentPasswordInputRef.current?.checkValidity()) {
      this.setState({errorMessage: "password is invalid (length is from 4 to 50 is required)"});
      return;
    }

    const newUsername:string=this.newUsernameInputRef.current?.value??'';
    const currentPassword:string=this.currentPasswordInputRef.current?.value??'';

    changeUsername(currentPassword, newUsername).then((res) => {
      if(res===true) {
        refreshTokens().then(res => {
          if(res===false) return;
          window.location.reload();
          window.location.replace("/auth/signIn");
        });
      } else {
        this.setState({errorMessage: "username or password is incorrect or this new username is beeing registered"});
      }
    });
  }

  render():React.ReactNode {
    const {errorMessage}=this.state;

    return <>
      <h3>changeUsername</h3>
      <label>
        <input ref={this.currentPasswordInputRef} type="password" placeholder="a current password" 
          required minLength={4} maxLength={50} /><br />
      </label><br />
      <label>
        <input ref={this.newUsernameInputRef} type="text" placeholder="a new username" 
          required pattern="[A-Za-z0-9_]{1,30}"/>
      </label>
      <p className={Styles.errorMessage}>{errorMessage}</p>
      <button type="submit" onClick={this.actionButtonClickHandle}>confirm</button>
    </>;
  }
}

interface StateChangePageType {
  auth:boolean
}
class ChangePage extends React.Component<unknown, StateChangePageType> {
  constructor(props: unknown) {
    super(props);

    this.state = {
      auth: false
    };
  }

  componentDidMount(): void {
    if(!validTokens()) return;

    this.setState({auth: true});
  }

  render():React.ReactNode {
    return <>
    <p 
      className={`${Styles.changeRegistrationPageLink} ${Styles.changeRegistrationPageLinkFirst}`}
    >
      <Link href="/auth/signIn">sign in</Link>
    </p>
    </>;
  }
}
