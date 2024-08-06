"use client";

import React from "react";
import Link from "next/link";
import Styles from "./page.module.css";

import { validTokens } from "@/api/userAPI";
import {
  getAllTotics,
  TextListEntity, getAllTexts, 
  addText, updateText, deleteText 
} from "@/api/localstatisticAPI";

export default function Page_({ params }: { params: { topic: string } }) {
  return <Page topic={params.topic} />
}

let reloadListNeeded:boolean=false;
interface PropsPageType {
  topic: string;
}
interface StatePageType {
  topic: string;
  errorMessage: string|null;
  showAddForm: boolean;
}
class Page extends React.Component<PropsPageType , StatePageType> {
  static async getInitialProps({ query }: { query: { topic: string } }) {
    return { topic: query.topic };
  }

  constructor(props: PropsPageType) {
    super(props);
    const {topic}=props;

    this.state= {
      topic: topic,
      errorMessage: null,
      showAddForm: false
    };
  }

  componentDidMount(): void {
    const{topic}=this.state;
    if(!validTokens()) window.location.replace("/auth/signIn");
    getAllTotics().then(res => {
      if(!res.map(e=>e.name).includes(topic)) this.setState({errorMessage: "topic does not exist"});
    });
  }

  showAddFormButtonOnClickHandle = ():void => {
    const {showAddForm}=this.state;
    this.setState({showAddForm: !showAddForm});
  }

  render():React.ReactNode {
    const {topic, errorMessage,showAddForm}=this.state;
    if(errorMessage!==null) {
      return <h3 className="global_mainHeader">{errorMessage}</h3>;
    }
    return <>
      <TextList topic={topic} />
      <button onClick={this.showAddFormButtonOnClickHandle}>add new text</button>
      {showAddForm?<AddNewTextForm topic={topic}/>:<></>}
    </>;
  }
}

interface PropsTextListType {
  topic: string;
}
interface StateTextListType {
  topic: string;
  texts:TextListEntity[];
  selectedToChangeTexts: Map<string,{newName:string, errorMessage:string}>;
}
class TextList extends React.Component<PropsTextListType,StateTextListType> {
  constructor(props: PropsTextListType) {
    super(props);
    const {topic}=props;

    this.state={
      topic: topic,
      texts:[],
      selectedToChangeTexts:new Map<string,{newName:string, errorMessage:string}>()
    };
  }

  componentDidMount(): void {
    const {topic}=this.state;
    getAllTexts(topic).then(res => {
      this.setState({texts: res});
    });
    setInterval(()=>{
      if(!reloadListNeeded) return;

      reloadListNeeded=false;
      getAllTexts(topic).then(res => {
        this.setState({texts: res});
      });
    }, 100);
  }

  textChangeButtonClickHandle = (textName:string):void => {
    const {selectedToChangeTexts}=this.state;
    if (selectedToChangeTexts.has(textName)) {
      selectedToChangeTexts.delete(textName);
    } else {
      selectedToChangeTexts.set(textName, {newName: "", errorMessage: ""});
    }
    this.setState({selectedToChangeTexts: selectedToChangeTexts});
  }
  textChangeNewNameInputOnChangeHandle = 
    (textName:string, newValue:string):void => {
    const {selectedToChangeTexts}=this.state;
    const newTopic:{newName: string,errorMessage: string} = 
      selectedToChangeTexts.get(textName)!;
    selectedToChangeTexts.set(
      textName, 
      {newName: newValue, errorMessage: newTopic.errorMessage}
    );
    this.setState({selectedToChangeTexts: selectedToChangeTexts});
  }
  textChangeConfirmButtonOnClickHandle = (textName:string):void => {
    const {selectedToChangeTexts, topic}=this.state;
    const newTextName:{newName: string,errorMessage: string} = 
      selectedToChangeTexts.get(textName)!;
    if(!newTextName.newName.match("^[A-Za-z0-9_]{1,50}$")) {
      selectedToChangeTexts.set(
        textName, 
        {
          newName: newTextName.newName, 
          errorMessage: "a new name must consist of A-Za-z0-9_ and have length from 1 to 50"
        }
      );
      this.setState({selectedToChangeTexts: selectedToChangeTexts});
      return;
    }
    selectedToChangeTexts.delete(textName);
    updateText(topic, textName, newTextName.newName, null).then(res => {
      if(res===false) {
        selectedToChangeTexts.set(
          textName, 
          {
            newName: newTextName.newName, 
            errorMessage: "a new name is already used"
          }
        );
        this.setState({selectedToChangeTexts: selectedToChangeTexts});
        return;
      }
      reloadListNeeded=true;
      this.setState({selectedToChangeTexts: selectedToChangeTexts});
    });
    
  }
  textDeleteButtonClickHandle = (textName:string):void => {
    const {topic}=this.state;
    deleteText(topic, textName).then(res => {
      if(res===false) return;

      reloadListNeeded=true;
    });
  }

  render():React.ReactNode {
    const {topic, texts, selectedToChangeTexts}=this.state;
    const texts_:React.ReactNode[]=[];
    for(let i:number=0;i<texts.length;++i) {
      texts_[i]=<li key={texts[i].name}>
        <Link href={`/statistic/data/topic/${topic}/text/${texts[i].name}`}>
          <span>{texts[i].name}</span>
        </Link>
        <button onClick={() => this.textChangeButtonClickHandle(texts[i].name)} 
          className={Styles.manipulationButton} title="change">=</button>
        {
          selectedToChangeTexts.has(texts[i].name)?<>
            <input onChange={
                ({target: value}: React.ChangeEvent<HTMLInputElement>) => 
                  this.textChangeNewNameInputOnChangeHandle(texts[i].name, value.value)
              } 
              type="text" placeholder="new name" />
            <button onClick={() => this.textChangeConfirmButtonOnClickHandle(texts[i].name)} 
              type="button">change</button>
          </>:<></>
        }
        <button onClick={() => this.textDeleteButtonClickHandle(texts[i].name)} 
          className={Styles.manipulationButton} title="delete">X</button>
        {
          selectedToChangeTexts.has(texts[i].name)?<>
            <span>{selectedToChangeTexts.get(texts[i].name)?.errorMessage}</span>
          </>:<></>
        }
      </li>
    }

    return <section>
      <h2 className="global_mainHeader">All texts for topic {topic}</h2>
      <ul className={Styles.ListUl}>{texts_}</ul>
    </section>;
  }
}

interface PropsAddNewTextFormType {
  topic:string;
}
interface StateAddNewTextFormType {
  topic:string;
  textNameInput:string;
  textContentTextArea:string;
  statusMessage:string|null;
}
class AddNewTextForm 
  extends React.Component<PropsAddNewTextFormType,StateAddNewTextFormType> {
  constructor(props: PropsAddNewTextFormType) {
    super(props);
    const {topic}=props;

    this.state={
      topic: topic,
      textNameInput:"",
      textContentTextArea:"",
      statusMessage:null
    };
  }

  textNameInputOnChangeHandle = 
    ({target: value}: React.ChangeEvent<HTMLInputElement>):void => {
    this.setState({textNameInput: value.value});
  }
  textContentTextAreaOnChangeHandle = 
    ({target: value}: React.ChangeEvent<HTMLTextAreaElement>):void => {
    this.setState({textContentTextArea: value.value});
  }
  addNewTextButtonClickHandle = ():void => {
    const {topic, textNameInput, textContentTextArea}=this.state;

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

    addText(topic, textNameInput, textContentTextArea).then(res => {
      if(res===false) {
        this.setState({statusMessage: "a text name is incorrect or is already used"});
        return;
      }

      this.setState({
        textNameInput: "", 
        textContentTextArea: "", 
        statusMessage: `a new text ${textNameInput} added`
      });
      reloadListNeeded=true;
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
      <button onClick={this.addNewTextButtonClickHandle}>add</button>
    </section>;
  }
}
