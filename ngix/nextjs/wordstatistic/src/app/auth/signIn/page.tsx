"use client";

import React from "react";
import Link from "next/link";
import Styles from "./page.module.css";

import {signIn, validTokens, deleteTokens} from "../../../api/userAPI";

export default class Page extends React.Component<unknown, unknown> {
  constructor(props: unknown) {
    super(props);

    this.state = {

    };
  }

  render():React.ReactNode {
    return <div className={Styles.mainContent}>
      <SignInForm />
      <ChangeRegistrationPage />
    </div>;
  }
}

interface StateSignInFormType {
  errorMessage?:string
}
class SignInForm extends React.Component<unknown, StateSignInFormType> {
  constructor(props: unknown) {
    super(props);

    this.state = {

    };
  }

  usernameInputRef:React.RefObject<HTMLInputElement> = React.createRef<HTMLInputElement>();
  passwordInputRef:React.RefObject<HTMLInputElement> = React.createRef<HTMLInputElement>();

  signInButtonClickHandle = ():void => {
    if (!this.usernameInputRef.current?.checkValidity()) {
      this.setState({
        errorMessage: 
          "username is invalid (length from 1 to 30 is required and allowed only A-Z, a-z, _, 0-9)"
      });
      return;
    }
    if (!this.passwordInputRef.current?.checkValidity()) {
      this.setState({errorMessage: "password is invalid (length is from 4 to 50 is required)"});
      return;
    }

    const username:string=this.usernameInputRef.current?.value??'';
    const password:string=this.passwordInputRef.current?.value??'';

    signIn(username, password).then((res) => {
      if(res===true) {
        window.location.replace("/");
      } else {
        this.setState({errorMessage: "username or password is incorrect"});
      }
    });
  }

  render():React.ReactNode {
    const {errorMessage}=this.state;

    return <>
      <h3>sign in</h3>
      <label>
        <input ref={this.usernameInputRef} type="text" placeholder="username" 
          required pattern="[A-Za-z0-9_]{1,30}"/>
      </label><br />
      <label>
        <input ref={this.passwordInputRef} type="password" placeholder="password" 
          required minLength={4} maxLength={50} /><br />
      </label>
      <p className={Styles.errorMessage}>{errorMessage}</p>
      <button type="submit" onClick={this.signInButtonClickHandle}>sign in</button>
    </>;
  }
}

interface StateChangeRegistrationPageType {
  auth:boolean
}
class ChangeRegistrationPage extends React.Component<unknown, StateChangeRegistrationPageType> {
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
    const {auth}=this.state;

    return <>
    <p 
      className={`${Styles.changeRegistrationPageLink} ${Styles.changeRegistrationPageLinkFirst}`}
    >
      <Link href="/auth/signUp">sign up</Link>
    </p>
    {auth&&
    <p 
      className={`${Styles.changeRegistrationPageLink} ${Styles.changeRegistrationPageLinkNotFirst}`}
    >
      <Link href="/auth/changeUsername">change username</Link>
    </p>
    }
    {auth&&
    <p 
      className={`${Styles.changeRegistrationPageLink} ${Styles.changeRegistrationPageLinkNotFirst}`}
    >
      <Link href="/auth/changePassword">change password</Link>
    </p>
    }
    {auth&&
    <p 
      className={`${Styles.changeRegistrationPageLink} ${Styles.changeRegistrationPageLinkNotFirst}`}
    >
      <Link href="/auth/deleteUser">delete this account</Link>
    </p>
    }
    {auth&&
    <p 
      className={`${Styles.changeRegistrationPageLink} ${Styles.changeRegistrationPageLinkNotFirst}`}
    >
      <Link href="/" onClick={() => {deleteTokens(); window.location.reload();}}>logout</Link>
    </p>
    }
    <p 
      className={`${Styles.changeRegistrationPageLink} ${Styles.changeRegistrationPageLinkNotFirst}`}
    >
      <Link href="/">main page</Link>
    </p>
    </>;
  }
}
