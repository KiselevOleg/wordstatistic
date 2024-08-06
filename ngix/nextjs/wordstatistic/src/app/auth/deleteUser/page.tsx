'use client'

import React from "react";
import Link from "next/link";
import Styles from "./page.module.css";

import {deleteUser, validTokens, deleteTokens} from "../../../api/userAPI";

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

  confirmOperationInputRef:React.RefObject<HTMLInputElement> = React.createRef<HTMLInputElement>();
  currentPasswordInputRef:React.RefObject<HTMLInputElement> = React.createRef<HTMLInputElement>();

  actionButtonClickHandle = ():void => {
    if (!this.confirmOperationInputRef.current?.checked) {
      this.setState({
        errorMessage: 
          "you need to confirm this action"
      });
      return;
    }
    if (!this.currentPasswordInputRef.current?.checkValidity()) {
      this.setState({errorMessage: "a password is invalid (length is from 4 to 50 is required)"});
      return;
    }

    const currentPassword:string=this.currentPasswordInputRef.current?.value??'';

    deleteUser(currentPassword).then((res) => {
      if(res===true) {
          deleteTokens();
          window.location.reload();
          window.location.replace("/auth/signIn");
      } else {
        this.setState({errorMessage: "incorrent password"});
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
        <input ref={this.confirmOperationInputRef} type="checkbox" /> 
        I confirm that this account with all its data will 
        be permanentaly deleted with no possibility for recovering.
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
