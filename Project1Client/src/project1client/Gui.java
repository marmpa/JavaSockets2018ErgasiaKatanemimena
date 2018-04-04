//Μάριος Δημήτρης μπαντόλας
//icsd15137
package project1client;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.*;

/**
 *
 * @author marmp
 */
public class Gui extends JFrame{
    private JPanel panel;//private μεταβλήτης διάφορων jcomponent
    private JButton chooseJButton,sendJButton;
    private JLabel fileJLabel;
    
    private Socket sock;//μεταβλητές σχετικά με το σέρβερ
    private File myFile;
    private String oldFileName;
    
    public Gui()
    {
        
        
        myFile=null;//αρχεικοποιώ με Null για να μην έχω πρόβλημα παρακάτω
        panel = new JPanel(new GridLayout(2,3));//φτιάχνω ένα jpanel και του δίνω ένα gridlayout
        fileJLabel = new JLabel("");//αρχικοποιώ το jlabel
        
        
        chooseJButton = new JButton("Select file");//αρχεικοποιώ ένα κουμπί
        chooseJButton.addActionListener(new ActionListener()
        {//φτιάχνω το actionListener και κάνω ovveride την actionPerformed
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser fc = new JFileChooser();//φτιάνω ένα αντικείενο τύπου JFilechooser για να διαλέγω αρχεία
                
                int i = fc.showOpenDialog(getContentPane());//δείχνω το JFileChooser στο χρήστη για να διαλέξει αρχείο
                
                if(i==JFileChooser.APPROVE_OPTION)
                {//αν αποδέχτηκε κάποι αρχείο
                    myFile = fc.getSelectedFile();//αποθηκέυω το αρχείο στην μεταβλητή myFile
                    fileJLabel.setText(myFile.getName());//βάζω στο label το όνομα
                }
            }
        });
        
        sendJButton = new JButton("Send file");//φτιάχνω το κουμπί για το send
        sendJButton.addActionListener(new ActionListener() 
        {//το actionlistener για το κουμπί send
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                TCPSend();//καλή τη συνάρτηση TCPSend()s
            }
        });
        
        this.setVisible(true);//κάνω κάποιες παρομετροποιήσεις όπως να φαίνεται το μέγεθος και τι να κάνω οταν πατίεται το κουμπί Χ
        this.setSize(400,400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel.add(chooseJButton);//προσθέτω τα Jcomponents(κουμπιά και jlabel) στο πάνελ
        panel.add(fileJLabel);
        panel.add(sendJButton);
        this.add(panel);//προσθέτω το πάνελ στο frame;
        
        
    }
    
    private void TCPSend()
    {
        if(myFile==null)
        {//εάν το αρχείο δεν έχει διαλεχτεί
            return;//σταμάτα
        }
        
        FileInputStream fis=null;//αρχεικοποιώ όλα τα stream
        BufferedInputStream bis=null;
        DataInputStream dis=null;
        DataOutputStream dos=null;
        BufferedReader instream=null;
        
        try
        {//δοκίμασε
            sock = new Socket("127.0.0.1",5656);//προσπαθεί να συνδεθεί στο σέρβερ
            
            byte[] fileByteArray = new byte[(int) myFile.length()];//φτιάχνω ένα πίνακα byte ανάλογα με το μέγεθος αρχείο
            
            //InputStream is = sock.getInputStream();
            fis = new FileInputStream(myFile);//αρχεικοποιώ το fileinputstream και του δίνω το αρχείο
            
            bis = new BufferedInputStream(fis);//αρχεικοποιώ το bufferedinput για να γράφω το file input stream
            dis = new DataInputStream(bis);//δημιoυργώ το datainputstream για να μπορέσω να χρεισιμοποιήσω της λειτουργίες του
            dis.readFully(fileByteArray,0,fileByteArray.length);//διαβάζω το αρχείο στο πίνακα bytes
            
            //Transferring  
            OutputStream os = sock.getOutputStream();//παίρνω και αποθηκεύω το output του socket
            
            dos = new DataOutputStream(os);//δηλώνω το dataoutput stream για να στείλω στείλω το αρχείο καθώ και το όνομα και την ιπ
            
            dos.writeUTF(myFile.getName());//στέλνω το όνομα με το writeUTF που το μετατρέπει σε byte
            dos.writeUTF(InetAddress.getLocalHost().getHostAddress());//στέλνω την ip με τον ίδιο τρόπο
            dos.writeLong(fileByteArray.length);//στέλνω τo μέγεθος του αρχείου
            dos.write(fileByteArray,0,fileByteArray.length);//γράφω όλο το αρχείο
            dos.flush();//αναγκάζω να τα στείλει όλα
            
            instream = new BufferedReader(
            new InputStreamReader(sock.getInputStream()));//δηλώνω το instream για να διαβάσω δεδομένα απο το server
            
            String strin;//δηλώνω μια μεταβλητή
            do
            {//while loop το οποίο περιμένει να έθρει μύνημα επυτιχίας
               strin = instream.readLine();//διαβάζω το stream 
            
                if(strin.equalsIgnoreCase("true"))
                {//αν ισούτε με true
                    JDialog dial = new JDialog(this,"File send succesfully",true);//φτιαξε το jdialog και τις αναλογες παραμέτρους
                    dial.setLocationRelativeTo(this);
                    dial.add(new JLabel("File "+ myFile.getName()+" send succesfully"));
                    dial.pack();
                    dial.setVisible(true);
                }
            }
            while(!strin.equalsIgnoreCase("true"));
            
            fis.close();//κλείνει όλες τις εισόδους
            bis.close();
            dis.close();
            dos.close();
            instream.close();
            sock.close();
            
            myFile=null;//μηδενίζει το αρχείο 
        }
        catch(IOException ex)
        {//ότανε γίνεται το io exception 
            System.out.println("Exception caught when creating the file");//μύνημα
            
            JDialog dial = new JDialog(this,"File send unsucesfully",true);//εμφανίζει το jdialog λάθους
            dial.setLocationRelativeTo(this);
            dial.add(new JLabel("File "+ myFile.getName()+" failed to be send"));
            dial.pack();
            dial.setVisible(true);
            
            //κλείνω τα πάντα ένα ένα
            try
            {
                fis.close();
                
            }
            catch(IOException ee)
            {
                
            }
            
            try
            {
                
                bis.close();
               
            }
            catch(IOException ee)
            {
                
            }
            
            try
            {
               
                dis.close();
                
            }
            catch(IOException ee)
            {
                
            }
            
            try
            {
               
                dos.close();
                
            }
            catch(IOException ee)
            {
                
            }
            
            try
            {
                
                instream.close();
            }
            catch(IOException ee)
            {
                
            }
            
            try
            {
                
                sock.close();//δοκιμάζει να κλήσει τα αρχεία
            }
            catch(IOException ee)
            {
                
            }
        }
        
    }
}
