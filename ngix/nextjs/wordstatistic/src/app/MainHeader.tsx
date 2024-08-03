"use client";

import React from "react";
import Link from 'next/link'
import Styles from "./layout.module.css";

interface PropsMainHeaderType {

}
interface StateMainHeaderType {

}
export default class MainHeader extends React.Component<PropsMainHeaderType, StateMainHeaderType> {
  constructor(props: PropsMainHeaderType) {
    super(props);

    this.state = {};
  }

  render():React.ReactNode {
    return <>
      <MainSiteTitle />
      <div className={Styles.emptySpace}></div>
      <Authorization />
    </>;
  }
}

interface PropsMainSiteTitleType {

}
interface StateMainSiteTitleType {

}
class MainSiteTitle extends React.Component<PropsMainSiteTitleType, StateMainSiteTitleType> {
  constructor(props: PropsMainSiteTitleType) {
    super(props);

    this.state = {};
  }

  render():React.ReactNode {
    return <span className={Styles.mainTitle}>WordStatistic</span>;
  }
}

interface PropsAuthorizationType {

}
interface StateAuthorizationType {
  userName?: string
}
class Authorization extends React.Component<PropsAuthorizationType, StateAuthorizationType> {
  constructor(props: PropsAuthorizationType) {
    super(props);

    this.state = {};
  }

  componentDidMount(): void {
    const userName:string|null=localStorage.getItem("userName");
    if(userName===null) return;
    this.setState(() => this.setState({userName: userName}));
  }

  render():React.ReactNode {
    const {userName}=this.state;
    return <div className={Styles.authorization}>
      <Link href="/signIn">
        <span className={userName!==undefined?Styles.authorizationUserName:Styles.authorizationSignIn}>
          {userName??"sign in"}
        </span>
      </Link>
    </div>;
  }
}
