"use client";

import React from "react";
//import Styles from "./page.module.css";
import {getMostPopularWords, addText} from "./../api/globalstatisticAPI";
import { validTokens, getTokenInformation } from "@/api/userAPI";
import ListOfMostPopularWords from "@/app/statistic/ListOfMostPopularWords";

interface StatePageType {
  auth: boolean
}
export default class Page extends React.Component<unknown, StatePageType> {
  constructor(props: unknown) {
    super(props);

    this.state = {
      auth: false
    };
  }

  componentDidMount(): void {
    if(validTokens()&&getTokenInformation()?.permissions.includes("addTextToGlobal")) {
      this.setState({auth:true});
    }
  }

  render():React.ReactNode {
    const {auth}=this.state;
    return <>
      {auth?<AddGlobalText />:<></>}
      <h2 className="global_mainHeader">List of the most popular words in all texts were ever loaded</h2>
      <ListOfMostPopularWords getStatisticFunction={getMostPopularWords}/>
    </>;
  }
}

interface StateAddGlobalTextType {
  statusSendingMessage: string|null,
  globalText:string
}
class AddGlobalText extends React.Component<unknown, StateAddGlobalTextType> {
  constructor(props: unknown) {
    super(props);

    this.state = {
      statusSendingMessage: null,
      globalText:""
    };
  }

  globalTextTextAreaRef:React.RefObject<HTMLTextAreaElement>=React.createRef<HTMLTextAreaElement>();
  sendGlobalTextButtonHandle = ():void => {
    const text:string|undefined = this.globalTextTextAreaRef.current?.value;
    if(!text) {
      this.setState({statusSendingMessage: "empty text error"});
      return;
    }
    if(text===undefined) return;

    addText(text).then(res=> {
      if(res===false) {
        this.setState({statusSendingMessage: "sending error"});
        return;
      }
      this.setState({statusSendingMessage: "sending success", globalText: ""});
    });
  }

  globalTextAreaOnChangeHandle = ({target: value}: React.ChangeEvent<HTMLTextAreaElement>):void => {
    this.setState({globalText: value.value});
  }

  render():React.ReactNode {
    const{statusSendingMessage, globalText}=this.state;
    return <>
      <textarea ref={this.globalTextTextAreaRef} onChange={this.globalTextAreaOnChangeHandle} 
        placeholder="text" value={globalText}></textarea><br />
      {(statusSendingMessage!==null)?<><span>{statusSendingMessage}</span><br /></>:<></>}
      <button onClick={this.sendGlobalTextButtonHandle}>send</button>
    </>;
  }
}
