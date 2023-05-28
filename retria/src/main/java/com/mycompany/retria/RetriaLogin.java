package com.mycompany.retria;

import com.github.britooo.looca.api.core.Looca;
import com.mycompany.retria.DAO.AdministradorDAO;
import com.mycompany.retria.services.Service;

import java.util.Scanner;

public class RetriaLogin {
    public static Service service = new Service();

    public static void main(String[] args) {
        Scanner leitor = new Scanner(System.in);
        AdministradorDAO admDAO = new AdministradorDAO();
        Looca looca = new Looca();


        Integer escolha = 0;
        String email;
        String senha;
        Boolean passou = false;

        service.plotarIntroducaoAscii();
        service.plotarIntroducao();


        service.plotarMenu();
        escolha = leitor.nextInt();


        switch (escolha) {
            case 1:
                System.out.println("Você escolheu ====> Realizar login");
                System.out.println("Insira o Email");
                email = leitor.next();
                leitor.nextLine();
                System.out.println("Insira a senha");
                senha = leitor.nextLine();

                if (admDAO.consultar(email, senha)) {
                    System.out.println("Email validao com sucesso! Iniciando programa!");
                    iniciarMonitoramento(email, senha);
                } else {
                    System.out.println("Email ou senha inválidos! Tente novamente!");
                }
                break;
            case 0:
                System.out.println("Encerrando o programa!");
                System.out.println("Obrigado por utilizar a retria!");
                System.out.println("Até logo!");
                break;
            default:
                System.out.println("Opção inválida! Tente novamente");
        }


    }

    public static void iniciarMonitoramento(String email, String senha) {
        service.scriptDeValidacaoDeBanco(email, senha);
        System.out.println("<=====Iniciando monitoramento=====>");
        try {
            service.validarMetrica();
        } catch (Exception ex) {
            ex.getMessage();
        }
    }
}
