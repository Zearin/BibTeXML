/*
 * $Id$
 *
 * Copyright (c) 2006 Moritz Ringler
 * This class is derived from EntryRetriever by Oskar Ojala
 * (also in this package)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sourceforge.texlipse.bibparser;

public class Author{
      
      private final String familyName;
      private final String[] givenNames;
      private final String junior;
      private final static java.util.regex.Pattern containsLowerCaseWord = 
        java.util.regex.Pattern.compile("(.*?)\\b([\\p{L}&&[^\\p{Lu}]].*)");
      
      public Author(String familyName, String[] givenNames, String junior){
          this.familyName = familyName.trim();
          this.givenNames = givenNames;
          this.junior = junior;
      }
      
      public Author(String bibTeXAuthor) throws java.text.ParseException{
          bibTeXAuthor = bibTeXAuthor.trim();
          String[] parts = bibTeXAuthor.split(" *, *",-1);
          switch(parts.length){
          case 3: //von Last, Junior, First
              familyName = parts[0];
              junior = parts[1];
              givenNames = parts[2].split("[ ~]", -1);
              break;
          case 2: //von Last, First
              familyName = parts[0];
              givenNames = parts[1].split("[ ~]", -1);
              junior = null;
              break;
          case 1: //First von Last 
              int lastBlank = bibTeXAuthor.lastIndexOf(' ');
              java.util.regex.Matcher m = containsLowerCaseWord.matcher(bibTeXAuthor);
              junior = null;
              if(m.matches()){
                  //we have a 'von' part and use its start
                  //as the beginning of the family name
                  familyName = m.group(2).trim();
                  String gN = m.group(1).trim();
                  givenNames = (gN.length() == 0)? new String[0] : gN.split("[ ~]");
              } else if(lastBlank != -1){
                  //we have no von part and use the last blank as the beginning
                  //of the family name 
                  familyName = bibTeXAuthor.substring(lastBlank + 1).trim();
                  givenNames = bibTeXAuthor.substring(0, lastBlank).split("[ ~]");
              } else {
                  //we have only a single-word family name
                  familyName = bibTeXAuthor;
                  givenNames = new String[0];
              }
              break;
          default:
              throw new java.text.ParseException("Cannot parse " + bibTeXAuthor + " as an author name.", 0);
          }
      }
      
      /** returns this author in "von Last, Jr, First" form */
      public String toString(){
          StringBuilder sb = new StringBuilder();
          sb.append(familyName);
          if(givenNames.length > 0){
              if(junior != null){
                  sb.append(", ").append(junior);
              }
              sb.append(", ").append(givenNames[0]);
              for(int i=1; i<givenNames.length; i++){
                  sb.append(' ').append(givenNames[i]);
              }
          }
          return sb.toString();
      }
      
      private boolean hasTwoWordFamilyName(){
          return (familyName.indexOf(' ') > 0) || (familyName.indexOf('~') > 0);
      }
}
