"use client";

import React from "react";
import Link from "next/link";
import Styles from "./page.module.css";

import {signIn, signUp} from "../../../api/userAPI";

export default class Page extends React.Component<unknown, unknown> {
  constructor(props: unknown) {
    super(props);

    this.state = {

    };
  }

  render():React.ReactNode {
    return <div className={Styles.mainContent}>
      <SignUpForm />
      <ChangeRegistrationPage />
    </div>;
  }
}

interface StateSignUpFormType {
  errorMessage?:string
}
class SignUpForm extends React.Component<unknown, StateSignUpFormType> {
  constructor(props: unknown) {
    super(props);

    this.state = {

    };
  }

  usernameInputRef:React.RefObject<HTMLInputElement> = React.createRef<HTMLInputElement>();
  passwordInputRef:React.RefObject<HTMLInputElement> = React.createRef<HTMLInputElement>();
  passwordCheckInputRef:React.RefObject<HTMLInputElement> = React.createRef<HTMLInputElement>();

  signUpButtonClickHandle = ():void => {
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
    if (this.passwordInputRef.current.value!=this.passwordCheckInputRef.current?.value) {
      this.setState({errorMessage: "a password and a check password are not equal"});
      return;
    }

    const username:string=this.usernameInputRef.current?.value??'';
    const password:string=this.passwordInputRef.current?.value??'';

    signUp(username, password).then((res) => {
      if(res===true) {
        return signIn(username, password);
      } else {
        this.setState({errorMessage: "username or password is incorrect or this username already exists"});
        return false;
      }
    }).then(res => {
      if(res===true) {
        window.location.replace("/");
      }
    });
  }

  render():React.ReactNode {
    const {errorMessage}=this.state;

    return <>
      <h3>sign up</h3>
      <label>
        <input ref={this.usernameInputRef} type="text" placeholder="username" 
          required pattern="[A-Za-z0-9_]{1,30}"/>
      </label><br />
      <label>
        <input ref={this.passwordInputRef} type="password" placeholder="password" 
          required minLength={4} maxLength={50} /><br />
      </label>
      <label>
        <input ref={this.passwordCheckInputRef} type="password" placeholder="check password" 
          required minLength={4} maxLength={50} /><br />
      </label>
      <p className={Styles.errorMessage}>{errorMessage}</p>
      <button type="submit" onClick={this.signUpButtonClickHandle}>sign up</button>
    </>;
  }
}

class ChangeRegistrationPage extends React.Component<unknown, unknown> {
  constructor(props: unknown) {
    super(props);

    this.state = {

    };
  }

  render():React.ReactNode {
    return <>
      <p 
        className={`${Styles.changeRegistrationPageLink} ${Styles.changeRegistrationPageLinkFirst}`}
      >
        <Link href="/auth/signIn">sign in</Link>
      </p>
      <p 
        className={`${Styles.changeRegistrationPageLink} ${Styles.changeRegistrationPageLinkNotFirst}`}
      >
        <Link href="/">main page</Link>
      </p>
    </>;
  }
}
