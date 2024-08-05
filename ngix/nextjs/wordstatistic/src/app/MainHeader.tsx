"use client";

import React from "react";
import Link from "next/link";
import Styles from "./layout.module.css";
import {getTokenInformation, validTokens} from "./../api/userAPI";

interface StateMainHeaderType {
  username: string|null,
  permissions: string[]|null
}
export default class MainHeader extends React.Component<unknown, StateMainHeaderType> {
  constructor(props: unknown) {
    super(props);

    this.state = {
      username: null,
      permissions: null
    };
  }

  componentDidMount(): void {
    if(!validTokens()) return;
    
    const {username, permissions}=getTokenInformation()??{username: null, permissions: null};
    if(username===null) return;
    this.setState(() => this.setState({username: username, permissions: permissions}));
  }

  render():React.ReactNode {
    const {username, permissions} = this.state;
    return <>
      <MainSiteTitle />
      <LinkList permissions={permissions}/>
      <Authorization username={username}/>
    </>;
  }
}

class MainSiteTitle extends React.Component<unknown, unknown> {
  constructor(props: unknown) {
    super(props);

    this.state = {};
  }

  render():React.ReactNode {
    return <span className={Styles.mainTitle}>WordStatistic</span>;
  }
}

interface PropsLinkListType {
  permissions: string[]|null
}
interface StateLinkListType {
  permissions: React.ReactNode[]
}
class LinkList extends React.Component<PropsLinkListType, StateLinkListType> {
  constructor(props: PropsLinkListType) {
    super(props);
    const {permissions} = props;

    this.state = {
      permissions: LinkList.generateLinkListFromPremissions(permissions)
    };
  }

  static getDerivedStateFromProps(
    nextProps: PropsLinkListType, 
    prevState: StateLinkListType
  ):StateLinkListType|null {
    const newPermissionsLinkList:React.ReactNode[]=
      LinkList.generateLinkListFromPremissions(nextProps.permissions);
    if (newPermissionsLinkList!==prevState.permissions) {
      return {
        permissions: newPermissionsLinkList
      };
    }
    return null;
  }

  render() {
    const {permissions}=this.state;

    return <div className={Styles.linkList}><ol className={Styles.linkListOl}>{permissions}</ol></div>;
  }

  static generateLinkListFromPremissions (permissions: string[]|null):React.ReactNode[] {
    return permissions
    //?.filter(e => e in ["addTextToGlobal","viewText","editText"])
    ?.filter(e => ["addTextToGlobal","viewText"].includes(e))
    ?.map(e => {
      switch(e) {
        case "addTextToGlobal":
          return <li key="addTextToGlobal" className={Styles.linkListLi}>
            <div><Link href="/">add new text to global</Link></div>
          </li>;
          break;
        case "viewText":
          return <>
            <li key="viewText_topics" className={Styles.linkListLi}>
            <div><Link href="/topics">own topics</Link></div>
            </li>
            <li key="viewText_userStatistic" className={Styles.linkListLi}>
            <div><Link href="/userStatistic">get statistic for all own texts</Link></div>
            </li>
          </>;
          break;
      }
    })??[];
  }
}

interface PropsAuthorizationType {
  username: string|null
}
interface StateAuthorizationType {
  username: string|null
}
class Authorization extends React.Component<PropsAuthorizationType, StateAuthorizationType> {
  constructor(props: PropsAuthorizationType) {
    super(props);
    const {username} = props;

    this.state = {
      username: username
    };
  }

  static getDerivedStateFromProps(
    nextProps: PropsAuthorizationType, 
    prevState: StateAuthorizationType
  ):StateAuthorizationType|null {
    if (nextProps.username !== prevState.username) {
      return {
        username: nextProps.username,
      };
    }
    return null;
  }

  render():React.ReactNode {
    const {username}=this.state;
    return <div className={Styles.authorization}>
      <Link href="/auth/signIn">
        <span className={username?Styles.authorizationUserName:Styles.authorizationSignIn}>
          {username??"sign in"}
        </span>
      </Link>
    </div>;
  }
}
