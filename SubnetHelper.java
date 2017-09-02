/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package subnet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Salvador Vera Franco 
 *  SubnetHelper class 1.1
 */
public class SubnetHelper {
    
    /**
     *
     */
    public static final int MASK_ADDRESS=1;

    /**
     *
     */
    public static final int SLASH_FORMAT_MASK=-1;

    protected static final String IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";

    protected static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);
    
    
    private long getTotalHost(long HOST,boolean ADD_ID_AND_BROADCAST,boolean return_exponent){
        long totalhost=0;
        long b=0;
        long HOST_2=HOST+(ADD_ID_AND_BROADCAST ? 2 : 0);
        int exponent=1;
      
        while((b != HOST_2)){
                if(exponent<33){
                    totalhost=(long)Math.pow(2,exponent)*((HOST_2 <= 256) ? 1 : 256);
                    b=Long.min(HOST_2, totalhost);
                    exponent++;
                }
                else break;
            }
            return (return_exponent ? (HOST_2 <= 256 ? exponent-1 : exponent+7) :totalhost);
        
       }
    
    /**
     * Returns the TOTALHOST to NHOST
     * @param NHOST Can be any number less than 2^32
     * @param ADD_ID_AND_BROADCAST if it's true sum 2 at NHOST
     * @return TOTALHOST or zero if HOST is wrong 
     */
    public long getTotalHost(long NHOST,boolean ADD_ID_AND_BROADCAST){
        long totalhost=0;
        long b=0;
        long HOST_2=NHOST+(ADD_ID_AND_BROADCAST ? 2 : 0);
        int exponent=1;
        if(HOST_2<=Math.pow(2,32)&&HOST_2>0){
            
            while((b != HOST_2)){
                if(exponent<33){
                    totalhost=(long)Math.pow(2,exponent)*((HOST_2 <= 256) ? 1 : 256);
                     b=Long.min(HOST_2,totalhost );
                    exponent++;
                }
                else break;
            }
        }
            return totalhost;
        
       }
    
     /**
     * Returns the NETMASK 
     * @param NHOST Can be any number less than 2^32
     * @param FORMAT Use variable SLASH_FORMAT_MASK or MASK_ADDRESS
     * @return TOTALHOST or zero if HOST is wrong 
     * @throws IllegalArgumentException if the FORMAT is invalid, or NHOST is out range
     */
    public String getNetMask(long NHOST,final int FORMAT){
        String RETURN="";
        String mask;
        if(NHOST>0&&NHOST<=Math.pow(2, 32)){
            
            mask=Long.toString(32-getTotalHost(NHOST, false, true));
            
                switch(FORMAT){
                case SLASH_FORMAT_MASK:
                    RETURN= "/"+mask;
                    break;
            
                case MASK_ADDRESS: 
                   
                    int trailingZeroes = 32 - rangeCheck(Integer.parseInt(mask), 0, 32);
                    RETURN=format(toArray((int) (0x0FFFFFFFFL << trailingZeroes )));;
                    break;
                    
                default: throw new IllegalArgumentException("Incorrect Format");
            }
        }else throw new IllegalArgumentException();
        
        return RETURN;
    }
    
    /**
     * Return Default mask.
     * @param ipaddress An IP address, e.g. "192.168.0.1"
     * @return  Default mask or "Not defined" 
     */
    public String DefaultMask(String ipaddress){
    
        String defmask="";
        int first=Integer.parseInt(ipaddress.substring(ipaddress.indexOf(""),ipaddress.indexOf(".")));
        if(first>=0&&first<128)
        defmask="255.0.0.0";
        else if(first>127&&first<192)
       defmask="255.255.0.0";
        else if(first>191&&first<224)
       defmask="255.255.255.0";
        else defmask="Not defined";
       
        return defmask;
    }
    
    /**
     *
     * @param array
     * @return
     */
    public String Summarize(String array[]){
        int first = 0,next,mask=32,bits; 
       
        Matcher matcher2 = addressPattern.matcher(array[0]);
        if(matcher2.matches()){
           first=SubnetHelper.matchAddress(matcher2);
           for(int x=1;x<array.length;x++){
                Matcher matcher = addressPattern.matcher(array[x]);
      
                    if(matcher.matches()){
                        
                        next=SubnetHelper.matchAddress(matcher);
                        first &=next;
                        bits=getUsedMaskBits(first, next);
                        mask=(bits<mask)?bits:mask;
                        // Obtener mask sumarizada
       
                    }
                    else     throw new IllegalArgumentException();
            }
         }return format(toArray(first))+"/"+mask;
       
    }
    
    
    private static int getUsedMaskBits(int NET_ADDRESS_1,int NET_ADDRESS_2){
        String netbits1=Integer.toBinaryString(NET_ADDRESS_1);
        String netbits2=Integer.toBinaryString(NET_ADDRESS_2);
        int bits=0;
        if(netbits1.length()==netbits2.length()){
            for(int x=0;x<netbits1.length();x++){
                if(netbits1.substring(0,x).equals(netbits2.substring(0,x)))
                    bits= x+(32-netbits1.length());
                else break;
                
            }
            
        }else throw new ArrayIndexOutOfBoundsException("Network address not match");
        
        return bits;
    }
    
  

    /**
     * Convenience function to check integer boundaries.
     * Checks if a value x is in the range [begin,end].
     * Returns x if it is in range, throws an exception otherwise.
     * @param value
     * @param begin
     * @param end
     * @return
     */

    protected static int rangeCheck(int value, int begin, int end) {
        if (value >= begin && value <= end) { 
            return value;
        }

        throw new IllegalArgumentException("Value [" + value + "] not in range ["+begin+","+end+"]");
    }
    
 

    /**
     * Convenience method to extract the components of a dotted decimal address and
     * pack into an integer using a regex match
     * @param matcher
     * @return
     */

    protected static int matchAddress(Matcher matcher) {
        int addr = 0;
        for (int i = 1; i <= 4; ++i) {
            int n = (rangeCheck(Integer.parseInt(matcher.group(i)), 0, 255));
            addr |= ((n & 0xff) << 8*(4-i));
            
        }
      
        return addr;
    }
    
  

    /**
     * Convert a packed integer address into a 4-element array
     * @param val
     * @return
     */

    protected int[] toArray(int val) {
        int ret[] = new int[4];
        
        for (int j = 3; j >= 0; --j) {
            ret[j] |= ((val >>> 8*(3-j)) & (0xff));
           
            
        }
         System.out.println("ret="+ret+"   val="+val);
        return ret;
    }
    


    /**
     * Convert a 4-element array into dotted decimal format
     * @param octets
     * @return
     */

    protected  String format(int[] octets) {
        StringBuilder str = new StringBuilder();
        for (int i =0; i < octets.length; ++i){
            str.append(octets[i]);
            if (i != octets.length - 1) {
                str.append(".");
            }
        }
        return str.toString();
    }
    
}
