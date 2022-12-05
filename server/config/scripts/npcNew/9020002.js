field = target.FieldSet( "Party1" ); 
 	if ( target.fieldID() == 103000805 ) { 
 		nRet = self.askYesNo( "Você caçou muito no mapa de bônus? Assim que sair deste lugar, você não poderá voltar e caçar de novo. Tem certeza de que deseja sair?" ); 
 		if ( nRet == 0 ) self.say( "Entendo. Este mapa foi feito para você caçar o máximo possível antes que o tempo acabe. Você precisa falar comigo se quiser sair deste estágio." ); 
 		else target.registerTransferField( 103000890, "" ); 
 	} 
 	else if ( target.fieldID() == 103000890 ) { 
 		inven = inventory; 
 		count = inven.itemCount( 4001007 ); 
 		if ( count > 0 ) { 
 			if ( inven.exchange( 0, 4001007, -count ) == 0 ) { 
 				self.say( "Tem certeza de que possui a quantidade exata de cupons? Por favor, verifique mais uma vez." ); 
 				end; 
 			} 
 		} 
 		count = inven.itemCount( 4001008 ); 
 		if ( count > 0 ) { 
 			if ( inven.exchange( 0, 4001008, -count ) == 0 ) { 
 				self.say( "Tem certeza de que possui a quantidade exata de passes? Por favor, verifique novamente." ); 
 				end; 
 			} 
 		} 
 		target.registerTransferField( 103000000, "" ); 
 	} 
 	else { 
 		// Send the user to the "mapa final" from every stage 
 		nRet = self.askYesNo( "Se sair do mapa, você vai precisar refazer toda a missão se quiser tentar novamente. Ainda quer sair deste mapa?"); 
 		if ( nRet == 0 ) self.say( "Entendo. O trabalho de equipe é muito importante aqui. Por favor, esforce-se mais com os membros do seu grupo." ); 
 		else target.registerTransferField( 103000890, "" ); 
 	} 