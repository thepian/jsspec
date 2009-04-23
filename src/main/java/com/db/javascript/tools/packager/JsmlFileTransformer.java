package com.db.javascript.tools.packager;

import au.id.jericho.lib.html.*;

import java.io.*;
import java.util.*;


/**
 * @author Maurice Nicholson
 */
public class JsmlFileTransformer implements FileTransformer {
    private boolean verbose = false;

    /*
    <li js:id="elItem"><a js:id="elAnchor"><span js:id="elLabel"></span></a></li>

    Jsml.registerTemplate("NavigationListItem",
        '<li ><a ><span ><\/span><\/a><\/li>',
        function(o,r){
            o.elItem=r;
            o.elAnchor=o.elItem[f];
            o.elLabel=o.elAnchor[f];
        },
        function(o,r,p){
            o[p+"ElItem"]=r;
            o[p+"ElAnchor"]=o[p+"ElItem"][f];
            o[p+"ElLabel"]=o[p+"ElAnchor"][f];
        },
        function(o,p){
            p=p||'';
            o[p+"elItem"]=null;
            o[p+"elAnchor"]=null;
            o[p+"elLabel"]=null;
        }
    );

        var f='firstChild',n='nextSibling',c='childNodes',p='parentNode';var E = Ext.get, EU = Ext.unbind;

Jsml.registerTemplate("GroupSummary",
'<table border="0" cellpadding="0" cellspacing="0" class="summaryViewTable" width="100%">
  <tbody>
    <tr class="row">
      <td class="name" width="150">Fund Manager<\/td>
      <td class="value bold" behaviour="crossDrillFundManager"><i><\/i><\/td>
      <td class="name" width="142">Fund Country<\/td><td class="value" width="140" ><i><\/i><\/td>
      <td class="name" width="142">PD Rating<\/td><td class="value" width="100" ><i><\/i><\/td>
      <td class="name" width="116">ISDA<\/td><td class="value" width="50" ><i><\/i><\/td>
    <\/tr>
    <tr class="row">
      <td class="name">Fund Mgr Org ID<\/td>
      <td class="value" ><\/td><td class="name">Fund Region<i><\/i><\/td>
      <td class="value" ><i><\/i><\/td>
      <td class="name">Tier<\/td>
      <td class="value" ><i><\/i><\/td>
      <td class="name">ISDA Netting<\/td>
      <td class="value" ><i><\/i><\/td>
    <\/tr>
    <tr class="row">
    <td class="name">Fund<\/td><td class="value" id="funds" ><i><\/i><\/td><td class="name">Industry<\/td><td class="value" ><i><\/i><\/td><td class="name">PFE Limit<\/td><td class="value" ><i><\/i><\/td><td class="name">PSA<\/td><td class="value" ><i><\/i><\/td><\/tr><tr class="row"><td class="name">Fund Org ID<\/td><td class="value" ><\/td><td class="name">PCO<\/td><td class="value" ><i><\/i><\/td><td class="name">PFE Availability<\/td><td class="value" behaviour="negativeValuesToRed"><i><\/i><\/td><td class="name">GMRA<\/td><td class="value" ><i><\/i><\/td><\/tr><tr class="row"><td class="name">Guarantor<\/td><td class="value"><i><\/i><\/td><td class="name">SCO<\/td><td class="value" ><i><\/i><\/td><td class="name">NAV<\/td><td class="value" ><i><\/i><\/td><td class="name">PB<\/td><td class="value" ><i><\/i><\/td><\/tr><tr class="row"><td class="name">Next Review Date<\/td><td class="value" ><i><\/i><\/td><td class="name">Team Region<\/td><td class="value" ><i><\/i><\/td><td class="name">NAV Date<\/td><td class="value" ><i><\/i><\/td><td class="name">Activity<\/td><td class="value" ><i><\/i><\/td><\/tr><tr class="row row-last"><td class="name" ><span>Comment<\/span><\/td><td class="value" ><\/td><td class="name">HF Strategy<\/td><td class="value" ><i><\/i><\/td><td class="name">Performance YTD<\/td><td class="value" ><i><\/i><\/td><td class="name">Master Netting<\/td><td class="value" ><i><\/i><\/td><\/tr><\/tbody><\/table>',

    function(o,r){
      o.labelTo = new Array();
      o.el=E(r);
      o.labelTo[0]=o.el.dom[f][f][f][f];
      o.fundManager=o.el.dom[f][f][c][1];
      o.labelTo[1]=o.fundManager[n][f];
      o.fundCountry=o.fundManager[p][c][3];
      o.labelTo[2]=o.fundManager[p][c][4][f];
      o.pdRating=o.fundManager[p][c][5];
      o.labelTo[3]=o.fundManager[p][c][6][f];
      o.isda=o.fundManager[p][c][7];o.labelTo[4]=o.el.dom[f][c][1][f][f];o.groupOrgId=o.el.dom[f][c][1][c][1];o.labelTo[5]=o.groupOrgId[n][f];o.fundRegion=o.groupOrgId[p][c][3];o.labelTo[6]=o.groupOrgId[p][c][4][f];o.tier=o.groupOrgId[p][c][5];o.labelTo[7]=o.groupOrgId[p][c][6][f];o.isdaNetting=o.groupOrgId[p][c][7];o.labelTo[8]=o.el.dom[f][c][2][f][f];o.funds=o.el.dom[f][c][2][c][1];o.labelTo[9]=o.funds[n][f];o.industry=o.funds[p][c][3];o.labelTo[10]=o.funds[p][c][4][f];o.pfeLimit=o.funds[p][c][5];o.labelTo[11]=o.funds[p][c][6][f];o.psa=o.funds[p][c][7];o.labelTo[12]=o.el.dom[f][c][3][f][f];o.fundOrgId=o.el.dom[f][c][3][c][1];o.labelTo[13]=o.fundOrgId[n][f];o.pco=o.fundOrgId[p][c][3];o.labelTo[14]=o.fundOrgId[p][c][4][f];o.pfeExcess=o.fundOrgId[p][c][5];o.labelTo[15]=o.fundOrgId[p][c][6][f];o.gmra=o.fundOrgId[p][c][7];o.labelTo[16]=o.el.dom[f][c][4][f][f];o.labelTo[17]=o.el.dom[f][c][4][c][2][f];o.sco=o.el.dom[f][c][4][c][3];o.labelTo[18]=o.sco[n][f];o.nav=o.sco[p][c][5];o.labelTo[19]=o.sco[p][c][6][f];o.pb=o.sco[p][c][7];o.labelTo[20]=o.el.dom[f][c][5][f][f];o.nextReviewDate=o.el.dom[f][c][5][c][1];o.labelTo[21]=o.nextReviewDate[n][f];o.teamRegion=o.nextReviewDate[p][c][3];o.labelTo[22]=o.nextReviewDate[p][c][4][f];o.navDate=o.nextReviewDate[p][c][5];o.labelTo[23]=o.nextReviewDate[p][c][6][f];o.activity=o.nextReviewDate[p][c][7];o.userCommentLabel=o.el.dom[f][c][6][f];o.labelTo[24]=o.userCommentLabel[f][f];o.userComment=o.userCommentLabel[n];o.labelTo[25]=o.userCommentLabel[p][c][2][f];o.strategy=o.userCommentLabel[p][c][3];o.labelTo[26]=o.userCommentLabel[p][c][4][f];o.perfYtd=o.userCommentLabel[p][c][5];o.labelTo[27]=o.userCommentLabel[p][c][6][f];o.masterNetting=o.userCommentLabel[p][c][7];},

      function(o,r,p){
      o[p+"LabelTo"] = new Array();
      o[p+"El"]=E(r);
      o[p+"LabelTo"][0]=o[p+"El"].dom[f][f][f][f];
      o[p+"FundManager"]=o[p+"El"].dom[f][f][c][1];
      o[p+"LabelTo"][1]=o[p+"FundManager"][n][f];
      o[p+"FundCountry"]=o[p+"FundManager"][p][c][3];o[p+"LabelTo"][2]=o[p+"FundManager"][p][c][4][f];o[p+"PdRating"]=o[p+"FundManager"][p][c][5];o[p+"LabelTo"][3]=o[p+"FundManager"][p][c][6][f];o[p+"Isda"]=o[p+"FundManager"][p][c][7];o[p+"LabelTo"][4]=o[p+"El"].dom[f][c][1][f][f];o[p+"GroupOrgId"]=o[p+"El"].dom[f][c][1][c][1];o[p+"LabelTo"][5]=o[p+"GroupOrgId"][n][f];o[p+"FundRegion"]=o[p+"GroupOrgId"][p][c][3];o[p+"LabelTo"][6]=o[p+"GroupOrgId"][p][c][4][f];o[p+"Tier"]=o[p+"GroupOrgId"][p][c][5];o[p+"LabelTo"][7]=o[p+"GroupOrgId"][p][c][6][f];o[p+"IsdaNetting"]=o[p+"GroupOrgId"][p][c][7];o[p+"LabelTo"][8]=o[p+"El"].dom[f][c][2][f][f];o[p+"Funds"]=o[p+"El"].dom[f][c][2][c][1];o[p+"LabelTo"][9]=o[p+"Funds"][n][f];o[p+"Industry"]=o[p+"Funds"][p][c][3];o[p+"LabelTo"][10]=o[p+"Funds"][p][c][4][f];o[p+"PfeLimit"]=o[p+"Funds"][p][c][5];o[p+"LabelTo"][11]=o[p+"Funds"][p][c][6][f];o[p+"Psa"]=o[p+"Funds"][p][c][7];o[p+"LabelTo"][12]=o[p+"El"].dom[f][c][3][f][f];o[p+"FundOrgId"]=o[p+"El"].dom[f][c][3][c][1];o[p+"LabelTo"][13]=o[p+"FundOrgId"][n][f];o[p+"Pco"]=o[p+"FundOrgId"][p][c][3];o[p+"LabelTo"][14]=o[p+"FundOrgId"][p][c][4][f];o[p+"PfeExcess"]=o[p+"FundOrgId"][p][c][5];o[p+"LabelTo"][15]=o[p+"FundOrgId"][p][c][6][f];o[p+"Gmra"]=o[p+"FundOrgId"][p][c][7];o[p+"LabelTo"][16]=o[p+"El"].dom[f][c][4][f][f];o[p+"LabelTo"][17]=o[p+"El"].dom[f][c][4][c][2][f];o[p+"Sco"]=o[p+"El"].dom[f][c][4][c][3];o[p+"LabelTo"][18]=o[p+"Sco"][n][f];o[p+"Nav"]=o[p+"Sco"][p][c][5];o[p+"LabelTo"][19]=o[p+"Sco"][p][c][6][f];o[p+"Pb"]=o[p+"Sco"][p][c][7];o[p+"LabelTo"][20]=o[p+"El"].dom[f][c][5][f][f];o[p+"NextReviewDate"]=o[p+"El"].dom[f][c][5][c][1];o[p+"LabelTo"][21]=o[p+"NextReviewDate"][n][f];o[p+"TeamRegion"]=o[p+"NextReviewDate"][p][c][3];o[p+"LabelTo"][22]=o[p+"NextReviewDate"][p][c][4][f];o[p+"NavDate"]=o[p+"NextReviewDate"][p][c][5];o[p+"LabelTo"][23]=o[p+"NextReviewDate"][p][c][6][f];o[p+"Activity"]=o[p+"NextReviewDate"][p][c][7];o[p+"UserCommentLabel"]=o[p+"El"].dom[f][c][6][f];o[p+"LabelTo"][24]=o[p+"UserCommentLabel"][f][f];o[p+"UserComment"]=o[p+"UserCommentLabel"][n];o[p+"LabelTo"][25]=o[p+"UserCommentLabel"][p][c][2][f];o[p+"Strategy"]=o[p+"UserCommentLabel"][p][c][3];o[p+"LabelTo"][26]=o[p+"UserCommentLabel"][p][c][4][f];o[p+"PerfYtd"]=o[p+"UserCommentLabel"][p][c][5];o[p+"LabelTo"][27]=o[p+"UserCommentLabel"][p][c][6][f];o[p+"MasterNetting"]=o[p+"UserCommentLabel"][p][c][7];},

      function(o,p){
        p=p||'';
        EU(o[p+"el"]);
        o[p+"el"]=null;
        o[p+"fundManager"]=null;
        o[p+"fundCountry"]=null;
        o[p+"pdRating"]=null;
        o[p+"isda"]=null;
        o[p+"groupOrgId"]=null;
        o[p+"fundRegion"]=null;
        o[p+"tier"]=null;
        o[p+"isdaNetting"]=null;
        o[p+"funds"]=null;
        o[p+"industry"]=null;
        o[p+"pfeLimit"]=null;
        o[p+"psa"]=null;
        o[p+"fundOrgId"]=null;
        o[p+"pco"]=null;
        o[p+"pfeExcess"]=null;
        o[p+"gmra"]=null;
        o[p+"sco"]=null;
        o[p+"nav"]=null;
        o[p+"pb"]=null;
        o[p+"nextReviewDate"]=null;
        o[p+"teamRegion"]=null;
        o[p+"navDate"]=null;
        o[p+"activity"]=null;
        o[p+"userCommentLabel"]=null;o[p+"userComment"]=null;o[p+"strategy"]=null;o[p+"perfYtd"]=null;o[p+"masterNetting"]=null;});

Jsml.registerTemplate("GroupSummary",
'<table border="0" cellpadding="0" cellspacing="0" class="summaryViewTable" width="100%"><tbody><tr class="row"><td class="name" width="150">Fund Manager</td><td class="value bold" behaviour="crossDrillFundManager"><i></i></td><td class="name" width="142">Fund Country</td><td class="value" width="140"><i></i></td><td class="name" width="142">PD Rating</td><td class="value" width="100"><i></i></td><td class="name" width="116">ISDA</td><td class="value" width="50"><i></i></td></tr><tr class="row"><td class="name">Fund Mgr Org ID</td><td class="value"></td><td class="name">${labelTo:"Fund Region"}<i></i></td><td class="value"><i></i></td><td class="name">Tier</td><td class="value"><i></i></td><td class="name">ISDA Netting</td><td class="value"><i></i></td></tr><tr class="row"><td class="name">Fund</td><td class="value"><i></i></td><td class="name">Industry</td><td class="value"><i></i></td><td class="name">PFE Limit</td><td class="value"><i></i></td><td class="name">PSA</td><td class="value"><i></i></td></tr><tr class="row"><td class="name">Fund Org ID</td><td class="value"></td><td class="name">PCO</td><td class="value"><i></i></td><td class="name">PFE Availability</td><td class="value" behaviour="negativeValuesToRed"><i></i></td><td class="name">GMRA</td><td class="value"><i></i></td></tr><tr class="row"><td class="name">Guarantor</td><td class="value"><i></i></td><td class="name">SCO</td><td class="value"><i></i></td><td class="name">NAV</td><td class="value"><i></i></td><td class="name">PB</td><td class="value"><i></i></td></tr><tr class="row"><td class="name">Next Review Date</td><td class="value"><i></i></td><td class="name">Team Region</td><td class="value"><i></i></td><td class="name">NAV Date</td><td class="value"><i></i></td><td class="name">Activity</td><td class="value"><i></i></td></tr><tr class="row row-last"><td class="name"><span>Comment</span></td><td class="value"></td><td class="name">HF Strategy</td><td class="value"><i></i></td><td class="name">Performance YTD</td><td class="value"><i></i></td><td class="name">Master Netting</td><td class="value"><i></i></td></tr></tbody></table>',
function(o,r){
o.el=E(r);
o.labelTo=r[f][f][f][f];
o.fundManager=r[f][f][c][1];
o.labelTo=r[f][f][c][2][f];
o.fundCountry=r[f][f][c][3];
o.labelTo=r[f][f][c][4][f];
o.pdRating=r[f][f][c][5];
o.labelTo=r[f][f][c][6][f];
o.isda=r[f][f][c][7];
o.labelTo=r[f][c][1][f][f];
o.groupOrgId=r[f][c][1][c][1];o.fundRegion=r[f][c][1][c][3];o.labelTo=r[f][c][1][c][4][f];o.tier=r[f][c][1][c][5];o.labelTo=r[f][c][1][c][6][f];o.isdaNetting=r[f][c][1][c][7];o.labelTo=r[f][c][2][f][f];o.funds=r[f][c][2][c][1];o.labelTo=r[f][c][2][c][2][f];o.industry=r[f][c][2][c][3];o.labelTo=r[f][c][2][c][4][f];o.pfeLimit=r[f][c][2][c][5];o.labelTo=r[f][c][2][c][6][f];o.psa=r[f][c][2][c][7];o.labelTo=r[f][c][3][f][f];o.fundOrgId=r[f][c][3][c][1];o.labelTo=r[f][c][3][c][2][f];o.pco=r[f][c][3][c][3];o.labelTo=r[f][c][3][c][4][f];o.pfeExcess=r[f][c][3][c][5];o.labelTo=r[f][c][3][c][6][f];o.gmra=r[f][c][3][c][7];o.labelTo=r[f][c][4][f][f];o.labelTo=r[f][c][4][c][2][f];o.sco=r[f][c][4][c][3];o.labelTo=r[f][c][4][c][4][f];o.nav=r[f][c][4][c][5];o.labelTo=r[f][c][4][c][6][f];o.pb=r[f][c][4][c][7];o.labelTo=r[f][c][5][f][f];o.nextReviewDate=r[f][c][5][c][1];o.labelTo=r[f][c][5][c][2][f];o.teamRegion=r[f][c][5][c][3];o.labelTo=r[f][c][5][c][4][f];o.navDate=r[f][c][5][c][5];o.labelTo=r[f][c][5][c][6][f];o.activity=r[f][c][5][c][7];o.userCommentLabel=r[f][c][6][f];o.labelTo=o.userCommentLabel[f][f];o.userComment=r[f][c][6][c][1];o.labelTo=r[f][c][6][c][2][f];o.strategy=r[f][c][6][c][3];o.labelTo=r[f][c][6][c][4][f];o.perfYtd=r[f][c][6][c][5];o.labelTo=r[f][c][6][c][6][f];o.masterNetting=r[f][c][6][c][7];},

function(o,r,p){o[p+"El"]=E(r);o[p+"LabelTo"]=r[f][f][f][f];o[p+"FundManager"]=r[f][f][c][1];o[p+"LabelTo"]=r[f][f][c][2][f];o[p+"FundCountry"]=r[f][f][c][3];o[p+"LabelTo"]=r[f][f][c][4][f];o[p+"PdRating"]=r[f][f][c][5];o[p+"LabelTo"]=r[f][f][c][6][f];o[p+"Isda"]=r[f][f][c][7];o[p+"LabelTo"]=r[f][c][1][f][f];o[p+"GroupOrgId"]=r[f][c][1][c][1];o[p+"FundRegion"]=r[f][c][1][c][3];o[p+"LabelTo"]=r[f][c][1][c][4][f];o[p+"Tier"]=r[f][c][1][c][5];o[p+"LabelTo"]=r[f][c][1][c][6][f];o[p+"IsdaNetting"]=r[f][c][1][c][7];o[p+"LabelTo"]=r[f][c][2][f][f];o[p+"Funds"]=r[f][c][2][c][1];o[p+"LabelTo"]=r[f][c][2][c][2][f];o[p+"Industry"]=r[f][c][2][c][3];o[p+"LabelTo"]=r[f][c][2][c][4][f];o[p+"PfeLimit"]=r[f][c][2][c][5];o[p+"LabelTo"]=r[f][c][2][c][6][f];o[p+"Psa"]=r[f][c][2][c][7];o[p+"LabelTo"]=r[f][c][3][f][f];o[p+"FundOrgId"]=r[f][c][3][c][1];o[p+"LabelTo"]=r[f][c][3][c][2][f];o[p+"Pco"]=r[f][c][3][c][3];o[p+"LabelTo"]=r[f][c][3][c][4][f];o[p+"PfeExcess"]=r[f][c][3][c][5];o[p+"LabelTo"]=r[f][c][3][c][6][f];o[p+"Gmra"]=r[f][c][3][c][7];o[p+"LabelTo"]=r[f][c][4][f][f];o[p+"LabelTo"]=r[f][c][4][c][2][f];o[p+"Sco"]=r[f][c][4][c][3];o[p+"LabelTo"]=r[f][c][4][c][4][f];o[p+"Nav"]=r[f][c][4][c][5];o[p+"LabelTo"]=r[f][c][4][c][6][f];o[p+"Pb"]=r[f][c][4][c][7];o[p+"LabelTo"]=r[f][c][5][f][f];o[p+"NextReviewDate"]=r[f][c][5][c][1];o[p+"LabelTo"]=r[f][c][5][c][2][f];o[p+"TeamRegion"]=r[f][c][5][c][3];o[p+"LabelTo"]=r[f][c][5][c][4][f];o[p+"NavDate"]=r[f][c][5][c][5];o[p+"LabelTo"]=r[f][c][5][c][6][f];o[p+"Activity"]=r[f][c][5][c][7];o[p+"UserCommentLabel"]=r[f][c][6][f];o[p+"LabelTo"]=o[p+"UserCommentLabel"][f][f];o[p+"UserComment"]=r[f][c][6][c][1];o[p+"LabelTo"]=r[f][c][6][c][2][f];o[p+"Strategy"]=r[f][c][6][c][3];o[p+"LabelTo"]=r[f][c][6][c][4][f];o[p+"PerfYtd"]=r[f][c][6][c][5];o[p+"LabelTo"]=r[f][c][6][c][6][f];o[p+"MasterNetting"]=r[f][c][6][c][7];},

function(o,p){p=o||'';EU(o[p+"el"]);o[p+"labelTo"]=null;o[p+"fundManager"]=null;o[p+"labelTo"]=null;o[p+"fundCountry"]=null;o[p+"labelTo"]=null;o[p+"pdRating"]=null;o[p+"labelTo"]=null;o[p+"isda"]=null;o[p+"labelTo"]=null;o[p+"groupOrgId"]=null;o[p+"fundRegion"]=null;o[p+"labelTo"]=null;o[p+"tier"]=null;o[p+"labelTo"]=null;o[p+"isdaNetting"]=null;o[p+"labelTo"]=null;o[p+"funds"]=null;o[p+"labelTo"]=null;o[p+"industry"]=null;o[p+"labelTo"]=null;o[p+"pfeLimit"]=null;o[p+"labelTo"]=null;o[p+"psa"]=null;o[p+"labelTo"]=null;o[p+"fundOrgId"]=null;o[p+"labelTo"]=null;o[p+"pco"]=null;o[p+"labelTo"]=null;o[p+"pfeExcess"]=null;o[p+"labelTo"]=null;o[p+"gmra"]=null;o[p+"labelTo"]=null;o[p+"labelTo"]=null;o[p+"sco"]=null;o[p+"labelTo"]=null;o[p+"nav"]=null;o[p+"labelTo"]=null;o[p+"pb"]=null;o[p+"labelTo"]=null;o[p+"nextReviewDate"]=null;o[p+"labelTo"]=null;o[p+"teamRegion"]=null;o[p+"labelTo"]=null;o[p+"navDate"]=null;o[p+"labelTo"]=null;o[p+"activity"]=null;o[p+"userCommentLabel"]=null;o[p+"labelTo"]=null;o[p+"userComment"]=null;o[p+"labelTo"]=null;o[p+"strategy"]=null;o[p+"labelTo"]=null;o[p+"perfYtd"]=null;o[p+"labelTo"]=null;o[p+"masterNetting"]=null;});


     */
    public void transform(SourceFile sourceFile, InputStream is, OutputStream os, Appender appender) throws IOException {
        String text = sanitizeJavascript(is);

        Source source = new Source(text);
        OutputDocument doc = new OutputDocument(source);

        if (verbose) {
            System.out.format("source file %s\n", sourceFile.getName());
        }

        // todo support multiple top-level nodes
        TreeNode tn = parseSource(source, sourceFile, doc);
        if (verbose) {
            dumpTreeNode(tn, 0);
        }

        String html = doc.toString().replaceAll("[\\\n\\\r][\\\t ]*", "").replace("/", "\\/").replaceAll("&nbsp;|&#160;", "").replace("LABEL_PLACEHOLDER", "&nbsp;");
        StringBuilder buf = new StringBuilder("Jsml.registerTemplate(\"").
            append(sourceFile.basename()).
            append("\",'").
            append(html).
            append("',");

        /*
        bindFunction, bindWithPrefixFunction, unbindFunction
        function(o,r){o.elContainer=E(r);
            o.elState=o.elContainer.dom[f];
            o.elMouse=o.elState[f];
            o.buttonElement=E(o.elMouse[c][1][f][f]);
            o.labelContainer=o.buttonElement.dom[f];
        }
        */

        buf.append("function(o,r){");
        for (List<TreeNode> nodes : tn.getNodesById().values()) {
            if (nodes.size() > 1) {
                buf.append("o.").append(nodes.get(0).getId()).append(" = new Array();");
            }
        }
        addBinds(tn, buf, false, new HashSet<TreeNode>());
        buf.append("},");

        /*
        function(o,r,p){
    o[p+"ElContainer"]=E(r);
    o[p+"ElState"]=o[p+"ElContainer"].dom[f];
    o[p+"ElMouse"]=o[p+"ElState"][f];
    o[p+"ButtonElement"]=E(o[p+"ElMouse"][c][1][f][f]);
    o[p+"LabelContainer"]=o[p+"ButtonElement"].dom[f];
}
         */
        buf.append("function(o,r,p){");
        for (List<TreeNode> nodes : tn.getNodesById().values()) {
            if (nodes.size() > 1) {
                buf.append("o[p+\"").append(nodes.get(0).getUpperId()).append("\"] = new Array();");
            }
        }
        addBinds(tn, buf, true, new HashSet<TreeNode>());
        buf.append("},");

        /*
function(o,p){
    p=p||'';
    EU(o[p+"elContainer"]);
    o[p+"elContainer"]=null;
    o[p+"elState"]=null;
    o[p+"elMouse"]=null;
    EU(o[p+"buttonElement"]);
    o[p+"buttonElement"]=null;
    o[p+"labelContainer"]=null;
}

         */
        buf.append("function(o,p){p=p||'';");
        addUnbinds(tn, buf, new HashSet<String>());
        buf.append("}");

        os.write(buf.toString().getBytes());

        os.write(");\n".getBytes());
    }

    private String sanitizeJavascript(InputStream is) throws IOException {
        StringBuilder text = new StringBuilder(is.available());
        Reader reader = new InputStreamReader(is);
        char[] buf = new char[4096];
        int len;
        while ((len = reader.read(buf)) > 0) {
            text.append(buf, 0, len);
        }

        // escape embedded js
        int index = -1;
        while ((index = text.indexOf("'", index + 1)) != -1) {
            text.replace(index, index + 1, "\\'");
            index++;
        }

        int start = -1;
        String jsPrefix = "${javascript:";
        while ((start = text.indexOf(jsPrefix, start + 1)) != -1) {
            int end = text.indexOf("}", start + 1);
            if (end == -1) {
//                logWarning(source, sourceFile, null, "Missing end bracket for javascript expression starting [" + parseText.substring(start, Math.min(parseText.length(), start + 30)) + "...]");
                break;
            }
            String js = text.substring(start + jsPrefix.length(), end);
//            System.out.format("Replacing js with %s\n", "'+" + js + "+'");
            text.replace(start, end + 1, "'+" + js + "+'");
        }

        return text.toString();
    }

    private void dumpTreeNode(TreeNode tn, int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
        System.out.println(tn.toString());
        if (tn.hasChildren()) {
            for (TreeNode child : tn.getChildren()) {
                dumpTreeNode(child, indent + 1);
            }
        }
    }

    private void addUnbinds(TreeNode treeNode, StringBuilder buf, Collection<String> doneById) {
        if (treeNode.hasId()) {
            String id = treeNode.getId();
            if (!doneById.contains(id)) {
                doneById.add(id);

                if (treeNode.isExtElement()) {
                    buf.append("EU(").append("o[p+\"").append(id).append("\"]);");
                }
                buf.append("o[p+\"").append(id).append("\"]").append("=null;");
            }
        }
        if (treeNode.hasChildren()) {
            for (TreeNode child : treeNode.getChildren()) {
                addUnbinds(child, buf, doneById);
            }
        }
    }

    private void addBinds(TreeNode treeNode, StringBuilder buf, boolean withPrefix, Collection<TreeNode> done) {
        if (treeNode.hasId()) {
//            buf.append("o.").append(treeNode.id).append("=");
            buf.append("o");
            if (withPrefix) {
                buf.append("[p+\"").append(treeNode.getUpperId()).append("\"]");
            } else {
                buf.append(".").append(treeNode.getId());
            }
            if (treeNode.isArray()) {
                buf.append("[").append(treeNode.getArrayIndex()).append("]");
            }
            buf.append("=");
            if (treeNode.isExtElement()) {
                buf.append("E(");
            }
            // todo swap this round so root check is first; more efficient that way but for backwards compatibility right now
//            if (treeNode.isRoot) {
//                buf.append("r");
//            } else {
            String path = getPathToNode(treeNode, withPrefix, done);
            if (path == null) {
                if (!treeNode.isRoot()) {
                    throw new IllegalStateException("Expected node to be root");
                }
                path = "r";
            }
            buf.append(path);
//            }
            if (treeNode.isExtElement()) {
                buf.append(")");
            }
            buf.append(";");
            done.add(treeNode);
        }
        if (treeNode.hasChildren()) {
            for (TreeNode child : treeNode.getChildren()) {
                addBinds(child, buf, withPrefix, done);
            }
        }
    }

    private String getPathToNode(TreeNode treeNode, boolean withPrefix, Collection<TreeNode> done) {
        // todo extract to symbolic path which another class can render as node navigation
        List<String> path = new ArrayList<String>();
        TreeNode child = treeNode, parent = child.getParent();
        while (parent != null) {
            int index = parent.indexOf(child);
            // next sibling of identified node?
            if (index > 0 && child.getPreviousSibling().hasId()) {
                TreeNode previousSibling = child.getPreviousSibling();
                if (done.contains(previousSibling)) {
                    String siblingReference = getIdentifiedNodeReference(withPrefix, previousSibling);
                    path.add("o" + siblingReference + "[n]");
                    break;
                }
            }
            // child of parent of identified node?
            if (parent.hasIdentifiedChildOtherThan(child)) {
                TreeNode identifiedSibling = parent.getFirstIdentifiedChildOtherThan(child);
                if (done.contains(identifiedSibling)) {
                    String siblingReference = getIdentifiedNodeReference(withPrefix, identifiedSibling);
                    path.add("o" + siblingReference + "[p][c][" + index + "]");
                    break;
                }
            }

            if (index == 0) {
                path.add("[f]");
            } else {
                path.add("[c][" + index + "]");
            }
            if (parent.hasId()) {
                String parentReference = getIdentifiedNodeReference(withPrefix, parent);
                path.add("o" + parentReference);
                break;
            } else if (parent.isRoot()) {
                path.add("r");
                break;
            }
            child = parent;
            parent = child.getParent();
        }
        if (path.isEmpty()) {
            return null;
        }
        Collections.reverse(path);
        StringBuilder buf = new StringBuilder();
        for (String item : path) {
            buf.append(item);
        }
        return buf.toString();
    }

    private String getIdentifiedNodeReference(boolean withPrefix, TreeNode node) {
        String reference;
        if (withPrefix) {
            reference = "[p+\"" + node.getUpperId() + "\"]";
        } else {
            reference = "." + node.getId();
        }
        if (node.isExtElement()) {
            reference += ".dom";
        }
        if (node.isArray()) {
            reference += "[" + node.getArrayIndex() + "]";
        }
        return reference;
    }

    private TreeNode parseSource(Source source, SourceFile sourceFile, OutputDocument doc) {
        int textBegin = source.getBegin();
        TreeNode treeNode = null;
        TreeNode root = null;
        for (Object o : source.findAllTags()) {
            Tag tag = (Tag) o;

            TreeNode textNode = null;
            int textEnd = tag.getBegin();
            String text = CharacterReference.decodeCollapseWhiteSpace(new Segment(source, textBegin, textEnd));
            if (text != null) {
                text = text.trim();
                if (text.length() > 0) {
                    textNode = new TreeNode(treeNode);
                    textNode.setName("text");
                    textNode.setContent(text);
                    parseText(source, sourceFile, doc, textNode, text, textBegin, textEnd);
                }
            }

            if (tag instanceof EndTag) {
                treeNode = treeNode.getParent();
            }

            if (tag instanceof StartTag) {
                // todo support multiple top-level nodes
                if (tag.getName().equals("!--")) {
                    if (root == null) {
                        // special case: discard leading comments
                        doc.replace(tag.getElement(), "");
                    } else {
                        // create a tree node for the comment but don't alter current
                        new TreeNode(treeNode).setName("comment");
                    }
                } else {
                    if (root == null) {
                        root = treeNode = new TreeNode();
                    } else {
                        treeNode = new TreeNode(treeNode);
                    }
                    parseTag(source, sourceFile, doc, treeNode, tag);

                    if (tag.getElement().getEndTag() == null) {
                        treeNode = treeNode.getParent();
                    }
                }
            }

            textBegin = tag.getEnd();
        }
        return root;
    }

    private void parseTag(Source source, SourceFile sourceFile, OutputDocument doc, TreeNode treeNode, Tag tag) {
        treeNode.setName(tag.getName());
        Element element = tag.getElement();
        Attributes attrs = element.getAttributes();
        if (attrs != null) {
            Map attrsMap = doc.replace(attrs, false);
            if (attrsMap.containsKey("id")) {
                String id = (String) attrsMap.get("id");
                logWarning(source, sourceFile, element, "Element has id=\"%s\". The id attribute is not allowed in JSML", id);
            }
            if (attrsMap.containsKey("js:id")) {
                String id = (String) attrsMap.remove("js:id");
                treeNode.setId(id);
            }
            if (attrsMap.containsKey("js:type")) {
                String type = (String) attrsMap.remove("js:type");
                if (!"ext".equals(type)) {
                    logWarning(source, sourceFile, element, "js:type not recognised: \"%s\". Only \"ext\" is allowed. Ignoring it", type);
                } else {
                    treeNode.setExtElement(true);
                }
            }
        }
    }

    private void parseText(Source source, SourceFile sourceFile, OutputDocument doc, TreeNode treeNode, String text, int textBegin, int textEnd) {
        if (text.startsWith("${") && text.endsWith("}")) {
            String id = text.substring(2, text.length() - 1);
            String defaultValue = null;
            int colon;
            if ((colon = id.indexOf(":\"")) != -1) {
                defaultValue = id.substring(colon + 2, id.length() - 1);
                id = id.substring(0, colon);
            }
            treeNode.setId(id);
//            treeNode.addChild(id);
            doc.replace(textBegin, textEnd, defaultValue != null ? defaultValue : "LABEL_PLACEHOLDER");
//                System.out.format("Found text label %s, default %s\n", id, defaultValue);
        }
    }

    private void logWarning(Source source, SourceFile sourceFile, Element element, String fmt, Object... args) {
        RowColumnVector rcv = source.getRowColumnVector(element.getBegin());
        Object[] formatArgs = new Object[args.length + 4];
        formatArgs[0] = sourceFile.getAbsolutePath();
        formatArgs[1] = rcv.getRow();
        formatArgs[2] = rcv.getColumn();
        formatArgs[3] = element.getName();
        for (int i = 0, len = args.length; i < len; i++) {
            formatArgs[i + 4] = args[i];
        }
        System.err.format("WARN: %s:%d:%d: <%s...> " + fmt + "\n", formatArgs);
    }

    // a simple AST for the JSML structure
    private static class TreeNode {
        private TreeNode parent;
        private boolean root = false;
        private boolean extElement = false;
        private String id;
        private String upperId;
        private List<TreeNode> children;
        private Map<String, List<TreeNode>> nodesById;
        private String name;
        private String text;

        public TreeNode() {
            root = true;
            nodesById = new HashMap<String, List<TreeNode>>();
        }

        public TreeNode(TreeNode parent) {
            parent.addChild(this);
        }

        public TreeNode(TreeNode parent, String id) {
            this(parent);
            setId(id);
        }

        public void addChild(TreeNode child) {
            if (children == null) {
                children = new ArrayList<TreeNode>();
            }
            children.add(child);
            child.parent = this;
            child.nodesById = nodesById;
        }

        public void addChild(String id) {
            TreeNode child = new TreeNode(this, id);
        }

        public void setId(String id) {
            this.id = id;
            this.upperId = id.substring(0, 1).toUpperCase() + (id.length() > 1 ? id.substring(1) : "");
            addNodeById(id, this);
        }

        public String getId() {
            return id;
        }

        public boolean hasId() {
            return id != null;
        }

        public String getUpperId() {
            return upperId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setContent(String text) {
            this.text = text;
        }

        protected void addNodeById(String id, TreeNode treeNode) {
            List<TreeNode> nodes = nodesById.get(id);
            if (nodes == null) {
                nodes = new ArrayList<TreeNode>();
                nodesById.put(id, nodes);
            }
            nodes.add(treeNode);
        }

        public boolean isArray() {
            return nodesById.get(id).size() > 1;
        }

        public int getArrayIndex() {
            return nodesById.get(id).indexOf(this);
        }

        public List<TreeNode> getChildren() {
            return children;
        }

        public boolean hasChildren() {
            return children != null && children.size() > 0;
        }

        public boolean hasIdentifiedChildOtherThan(TreeNode child) {
            return getFirstIdentifiedChildOtherThan(child) != null;
        }

        public TreeNode getFirstIdentifiedChildOtherThan(TreeNode child) {
            if (children == null) {
                return null;
            }
            for (TreeNode c : children) {
                if (c != child && c.hasId()) {
                    return c;
                }
            }
            return null;
        }

        public int indexOf(TreeNode child) {
            return children.indexOf(child);
        }

        public TreeNode getPreviousSibling() {
            return parent.children.get(parent.indexOf(this) - 1);
        }

        public boolean isRoot() {
            return root;
        }

        public boolean isExtElement() {
            return extElement;
        }

        public void setExtElement(boolean extElement) {
            this.extElement = extElement;
        }

        public Map<String, List<TreeNode>> getNodesById() {
            return nodesById;
        }

        public TreeNode getParent() {
            return parent;
        }

        public String toString() {
            return name + (id != null ? "#" + id : "") + (text != null ? ": [" + text + "]" : "");
        }
    }
}

