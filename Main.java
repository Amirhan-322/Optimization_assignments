import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("S: ");
        double[] supply = readVector(scanner);

        System.out.println("C:");
        double[][] costMatrix = readMatrix(scanner, supply.length);

        System.out.print("D: ");
        double[] demand = readVector(scanner);

        checkBalance(supply,demand);
        printInputTable(costMatrix, supply, demand);
        System.out.println("Initial basic feasible solutions:");
        northWest(costMatrix,supply,demand);
        vogel(costMatrix,supply,demand);
        russell(costMatrix,supply,demand);

    }

    private static void checkBalance(double[] supply, double[] demand){
        double value = 0;
        for(int i = 0; i < 3; i++){
            value+=supply[i];
        }
        for(int i = 0; i < 4; i++){
            value-=demand[i];
        }
        if(value!=0){
            System.out.println("The problem is not balanced!");
            System.exit(0);
        }
    }


    private static void printInputTable(double[][] costMatrix, double[] supply, double[] demand) {
        int rows = costMatrix.length;
        int cols = costMatrix[0].length;

        System.out.print("     ");
        for (int j = 0; j < cols; j++) {
            System.out.printf("D%d  ", j + 1);
        }
        System.out.println("Supply");

        for (int i = 0; i < rows; i++) {
            System.out.printf("S%d  ", i + 1);
            for (int j = 0; j < cols; j++) {
                System.out.printf("%.3f  ", costMatrix[i][j]);
            }
            System.out.printf("%.3f\n", supply[i]);
        }

        System.out.print("Demand: ");
        for (int j = 0; j < cols; j++) {
            System.out.printf("%.3f  ", demand[j]);
        }
        System.out.println();
        System.out.println();
    }

    private static double[] readVector(Scanner scanner) {
        String input = scanner.nextLine();
        String[] parts = input.split(" ");
        double[] vector = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Double.parseDouble(parts[i]);
        }
        return vector;
    }


    public static double[][] readMatrix(Scanner scanner, int rows) {
        double[][] matrix = new double[rows][];
        for (int i = 0; i < rows; i++) {
            String[] input = scanner.nextLine().split(" ");
            matrix[i] = new double[input.length];
            for (int j = 0; j < input.length; j++) {
                matrix[i][j] = Double.parseDouble(input[j]);
            }
        }
        return matrix;
    }

    public static void printMatrix(double[][] matrix) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                System.out.printf("%.3f ", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    public static double[][] northWest(double[][] cost, double[] supply, double[] demand){
        double[][] result = new double[3][4];
        double[] newDemand = new double[4];
        double[] newSupply = new double[3];
        for(int i = 0; i<3; i++){
            for(int j = 0; j<4; j++){
                result[i][j] = 0;
            }
            newSupply[i] = supply[i];
        }
        for(int j = 0; j<4; j++){
            newDemand[j] = demand[j];
        }
        int curSup = 0,curDem = 0;

        while(curSup <= 2 && curDem <=3){

            if(newDemand[curDem]<newSupply[curSup]){
                result[curSup][curDem] = newDemand[curDem];
                newSupply[curSup] = newSupply[curSup]-newDemand[curDem];
                newDemand[curDem] = 0;
                curDem++;
            } else if (newDemand[curDem]>newSupply[curSup]) {
                result[curSup][curDem] = newSupply[curSup];
                newDemand[curDem] = newDemand[curDem]-newSupply[curSup];
                newSupply[curSup] = 0;
                curSup++;
            } else {
                result[curSup][curDem] = newSupply[curSup];
                curSup++;
                curDem++;
            }
        }
        System.out.println("1.Using North-West corner method:");
        printMatrix(result);
        return result;
    }

    public static void findDiff(double[] ColDif, double[] RowDif, double[][] cost, double[] newDemand, double[] newSupply) {
        for(int i = 0; i < 4; i++){
            if(newDemand[i]==0){
                ColDif[i]=-1;
                continue;
            }
            double min1 = Double.MAX_VALUE;
            double min2 = Double.MAX_VALUE;
            for(int j = 0; j < 3; j++){
                if(min2>cost[j][i] && newSupply[j]!=0){
                    min2 = cost[j][i];
                    if(min1>min2){
                        min2 = min1;
                        min1 = cost[j][i];
                    }
                }
            }
            if(min2 != Double.MAX_VALUE) {
                ColDif[i] = min2 - min1;
            }else{
                ColDif[i] = Double.MAX_VALUE;
            }
        }
        for(int i = 0; i < 3; i++){
            if(newSupply[i]==0){
                RowDif[i]=-1;
                continue;
            }
            double min1 = Double.MAX_VALUE;
            double min2 = Double.MAX_VALUE;
            for(int j = 0; j < 4; j++){
                if(min2>cost[i][j] && newDemand[j]!=0){
                    min2 = cost[i][j];
                    if(min1>min2){
                        min2 = min1;
                        min1 = cost[i][j];
                    }
                }
            }
            if(min2 != Double.MAX_VALUE) {
                RowDif[i] = min2-min1;
            }else{
                RowDif[i] = Double.MAX_VALUE;
            }

        }
    }

    public static double[][] vogel(double[][] cost, double[] supply, double[] demand){
        double[][] result = new double[3][4];
        double[] newDemand = new double[4];
        double[] newSupply = new double[3];
        for(int i = 0; i<3; i++){
            for(int j = 0; j<4; j++){
                result[i][j] = 0;
            }
            newSupply[i] = supply[i];
        }
        for(int j = 0; j<4; j++){
            newDemand[j] = demand[j];
        }
        double[] ColDif = new double[4];
        double[] RowDif = new double[3];
        while(true){
            findDiff(ColDif,RowDif,cost,newDemand,newSupply);
            double maxDif=-1;
            int index = -1;
            boolean isCol = true;
            for(int i = 0; i < 3; i++){
                if(RowDif[i]>maxDif){
                    maxDif = RowDif[i];
                    isCol = false;
                    index = i;
                }
            }
            for(int i = 0; i < 4; i++){
                if(ColDif[i]>maxDif){
                    maxDif = ColDif[i];
                    isCol = true;
                    index = i;
                }
            }
            int curSup = -1,curDem = -1;
            if(isCol){
                curDem = index;
                double min = Double.MAX_VALUE;
                for(int i = 0; i < 3; i++) {
                    if (cost[i][curDem] < min && RowDif[i] != -1) {
                        min = cost[i][curDem];
                        curSup = i;
                    }
                }
                if(newDemand[curDem]<newSupply[curSup]){
                    result[curSup][curDem] = newDemand[curDem];
                    newSupply[curSup] = newSupply[curSup]-newDemand[curDem];
                    newDemand[curDem] = 0;
                } else if (newDemand[curDem]>newSupply[curSup]) {
                    result[curSup][curDem] = newSupply[curSup];
                    newDemand[curDem] = newDemand[curDem]-newSupply[curSup];
                    newSupply[curSup] = 0;
                } else {
                    result[curSup][curDem] = newSupply[curSup];
                    newDemand[curDem] = 0;
                    newSupply[curSup] = 0;
                }
            }else{
                curSup = index;
                double min = Double.MAX_VALUE;
                for(int i = 0; i < 4; i++) {
                    if (cost[curSup][i] < min && ColDif[i] != -1) {
                        min = cost[curSup][i];
                        curDem = i;
                    }
                }
                if(newDemand[curDem]<newSupply[curSup]){
                    result[curSup][curDem] = newDemand[curDem];
                    newSupply[curSup] = newSupply[curSup]-newDemand[curDem];
                    newDemand[curDem] = 0;
                } else if (newDemand[curDem]>newSupply[curSup]) {
                    result[curSup][curDem] = newSupply[curSup];
                    newDemand[curDem] = newDemand[curDem]-newSupply[curSup];
                    newSupply[curSup] = 0;
                } else {
                    result[curSup][curDem] = newSupply[curSup];
                    newDemand[curDem] = 0;
                    newSupply[curSup] = 0;
                }
            }

            boolean Flag = true;
            for(int i = 0;i < 3; i++){
                if(newSupply[i] !=0){
                    Flag = false;
                    break;
                }
            }
            for(int i = 0;i < 4; i++){
                if(!Flag){
                    break;
                }
                if(newDemand[i] !=0){
                    Flag = false;
                }
            }
            if(Flag){
                break;
            }
        }

        System.out.println("2.Using Vogel’s approximation method:");
        printMatrix(result);
        return result;
    }


    public static double[][] russell(double[][] cost, double[] supply, double[] demand) {
        double[][] result = new double[3][4];
        double[] newDemand = new double[4];
        double[] newSupply = new double[3];
        for(int i = 0; i<3; i++){
            for(int j = 0; j<4; j++){
                result[i][j] = 0;
            }
            newSupply[i] = supply[i];
        }
        for(int j = 0; j<4; j++){
            newDemand[j] = demand[j];
        }
        double[] U = new double[3];
        double[] V = new double[4];
        while(true){
            for(int i = 0; i < 3; i++){
                if(newSupply[i]==0){
                    continue;
                }
                U[i]=Double.MIN_VALUE;
                for(int j = 0; j < 4; j++){
                    if(newDemand[j]==0){
                        continue;
                    }
                    if(U[i]<cost[i][j] && newSupply[i]!=0 && newDemand[j]!=0){
                        U[i]=cost[i][j];
                    }
                }
            }
            for(int i = 0; i < 4; i++){
                if(newDemand[i]==0){
                    continue;
                }
                V[i]=Double.MIN_VALUE;
                for(int j = 0; j < 3; j++){
                    if(newSupply[j]==0){
                        continue;
                    }
                    if(V[i]<cost[j][i]){
                        V[i]=cost[j][i];
                    }
                }
            }

            double max = 0;
            int curSup = -1, curDem = -1;
            for(int i = 0; i < 3; i++){
                if(newSupply[i]==0){
                    continue;
                }
                for(int j = 0; j < 4; j++){
                    if(newDemand[j]==0){
                        continue;
                    }
                    double value = Math.abs(cost[i][j] - U[i] - V[j]);
                    if(value>max){
                        max = value;
                        curSup = i;
                        curDem = j;
                    }
                }
            }

            if(newDemand[curDem]<newSupply[curSup]){
                result[curSup][curDem] = newDemand[curDem];
                newSupply[curSup] = newSupply[curSup]-newDemand[curDem];
                newDemand[curDem] = 0;
            } else if (newDemand[curDem]>newSupply[curSup]) {
                result[curSup][curDem] = newSupply[curSup];
                newDemand[curDem] = newDemand[curDem]-newSupply[curSup];
                newSupply[curSup] = 0;
            } else {
                result[curSup][curDem] = newSupply[curSup];
                newDemand[curDem] = 0;
                newSupply[curSup] = 0;
            }

            boolean Flag = true;
            for(int i = 0;i < 3; i++){
                if(newSupply[i] !=0){
                    Flag = false;
                    break;
                }
            }
            for(int i = 0;i < 4; i++){
                if(!Flag){
                    break;
                }
                if(newDemand[i] !=0){
                    Flag = false;
                }
            }
            if(Flag){
                break;
            }

        }

        System.out.println("3.Using Russell’s approximation method:");
        printMatrix(result);
        return result;
    }
}