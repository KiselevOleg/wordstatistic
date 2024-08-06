"use client";

import React from "react";
import Link from "next/link";
import Styles from "./page.module.css";

import { validTokens } from "@/api/userAPI";
import { getText, updateText } from "@/api/localstatisticAPI";

export default function Page_({ params }: { params: { topic: string, text: string } }) {
  return <Page topic={params.topic} text={params.text} />
}

interface PropsPageType {
  topic: string;
  text: string;
}
interface StatePageType {
  topic: string;
  text: string;
  textContent: string;
  errorMessage: string|null;
}
class Page extends React.Component<PropsPageType , StatePageType> {
  constructor(props: PropsPageType) {
    super(props);
    const {topic, text}=props;

    this.state= {
      topic: topic,
      text: text,
      textContent: "",
      errorMessage: null
    };
  }

  componentDidMount(): void {
    const{topic, text: textName}=this.state;
    if(!validTokens()) window.location.replace("/auth/signIn");
    getText(topic, textName).then(res => {
      if(res===false) {
        this.setState({errorMessage: "text does not exist"});
        return;
      }

      this.setState({textContent: res.text});
    });
  }

  render():React.ReactNode {
    const {topic, text, textContent, errorMessage}=this.state;
    if(errorMessage!==null) {
      return <h3 className="global_mainHeader">{errorMessage}</h3>;
    }
    return <>
      <TextForm topic={topic} textName={text} textContent={textContent}/>
    </>;
  }
}

interface PropsTextFormType {
  topic:string;
  textName: string;
  textContent: string;
}
interface StateTextFormType {
  topic:string;
  text: string;
  textNameInput:string;
  textContentTextArea:string;
  statusMessage:string|null;
}
class TextForm 
  extends React.Component<PropsTextFormType,StateTextFormType> {
  constructor(props: PropsTextFormType) {
    super(props);
    const {topic, textName, textContent}=props;

    this.state={
      topic: topic,
      text: textName,
      textNameInput:textName,
      textContentTextArea:textContent,
      statusMessage:null
    };
  }
  componentDidUpdate(prevProps: Readonly<PropsTextFormType>): void {
    if(prevProps.textContent!==this.props.textContent) this.setState({textContentTextArea: this.props.textContent});
  }

  textNameInputOnChangeHandle = 
    ({target: value}: React.ChangeEvent<HTMLInputElement>):void => {
    this.setState({textNameInput: value.value});
  }
  textContentTextAreaOnChangeHandle = 
    ({target: value}: React.ChangeEvent<HTMLTextAreaElement>):void => {
    this.setState({textContentTextArea: value.value});
  }
  changeTextButtonClickHandle = ():void => {
    const {topic, text, textNameInput, textContentTextArea}=this.state;

    if(!textNameInput.match("^[a-zA-Z0-9_]{1,50}$")) {
      this.setState({
        statusMessage: "a text name must contains of a-zA-Z0-9_ and has length from 1 to 50"
      });
      return;
    }
    if(textContentTextArea.length==0) {
      this.setState({
        statusMessage: "a text content must not be empty"
      });
      return;
    }

    updateText(topic, text, textNameInput, textContentTextArea).then(res => {
      if(res===false) {
        this.setState({statusMessage: "a text name is incorrect or is already used"});
        return;
      }

      window.location.replace(`/statistic/data/topic/${topic}/text/${textNameInput}`);
    });
  }

  render():React.ReactNode {
    const {topic, textNameInput, textContentTextArea, statusMessage}=this.state;

    return <section>
      <h2 className="global_mainHeader">Add a new text to a topic {topic}</h2>
      <input onChange={this.textNameInputOnChangeHandle} 
        type="text" placeholder="a text name" value={textNameInput}/><br />
      <textarea onChange={this.textContentTextAreaOnChangeHandle} value={textContentTextArea} /><br />
      {statusMessage!==null?<><span>{statusMessage}</span><br /></>:<></>}
      <button onClick={this.changeTextButtonClickHandle}>update text</button>

      <p 
        className={Styles.inPreviousPageLink}
      >
        <Link href={`/statistic/data/topic/${topic}/texts`}>on texts list page</Link>
      </p>
    </section>;
  }
}
