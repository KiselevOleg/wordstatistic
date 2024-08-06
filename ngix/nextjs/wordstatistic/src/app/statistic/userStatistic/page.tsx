"use client";

import React from "react";
import { validTokens } from "@/api/userAPI";
import { getMostPopularWordsForUser } from "@/api/localstatisticAPI";
import ListOfMostPopularWords from "../ListOfMostPopularWords";

export default class Page extends React.Component<unknown , unknown> {
  constructor(props: unknown) {
    super(props);

    this.state= {
      
    };
  }

  componentDidMount(): void {
    if(!validTokens()) window.location.replace('/auth/signIn');
  }

  render():React.ReactNode {
    return <>
      <h2 className="global_mainHeader">List of the most popular words in all own texts</h2>
      <ListOfMostPopularWords getStatisticFunction={getMostPopularWordsForUser}/>
    </>;
  }
}
