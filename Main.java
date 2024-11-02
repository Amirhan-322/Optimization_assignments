import java.util.Scanner;
import java.util.Arrays;

public class Main {
    private static final double EPSILON = 1e-6;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Input coefficients for the objective function
        System.out.println("C (coefficients of objective function): ");
        double[] C = inputVector(scanner);

        System.out.print("Number of constraints (n): ");
        int numConstraints = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("A (constraints matrix): ");
        double[][] A = inputMatrix(scanner, numConstraints);

        System.out.println("b (right-hand side of constraints): ");
        double[] b = inputVector(scanner);

        System.out.print("eps (tolerance): ");
        double eps = scanner.nextDouble();

        double[] x0 = generateInitialPoint(A, b, C.length);

        // Execute the Interior-Point Method with α = 0.5 and α = 0.9
        System.out.println("\nRunning Interior-Point Method for α = 0.5:");
        method(x0, 0.5, eps, b, C, A);

        System.out.println("\nRunning Interior-Point Method for α = 0.9:");
        method(x0, 0.9, eps, b, C, A);

        // Execute the Simplex Method
        System.out.println("\n----------");
        System.out.println("Running Simplex Method:");
        simplexMethod(C, A, b, C.length ,numConstraints);
    }

    public static double[] generateInitialPoint(double[][] A, double[] b, int numVariables) {
        double[] x0 = new double[numVariables];
        for (int i = 0; i < numVariables; i++) {
            x0[i] = 1.0;
        }
        return x0;
    }

    public static double[] inputVector(Scanner scanner) {
        String input = scanner.nextLine();
        String[] parts = input.split(" ");
        double[] vector = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Double.parseDouble(parts[i]);
        }
        return vector;
    }

    public static double[][] inputMatrix(Scanner scanner, int rows) {
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

    public static void method(double[] x0, double alpha, double eps, double[] b, double[] C, double[][] A) {
        boolean sol = true;
        for (double[] row : A) {
            boolean neg = true;
            for (double value : row) {
                if (value > 0) {
                    neg = false;
                }
            }
            if (neg) {
                sol = false;
            }
        }
        if (!sol) {
            System.out.println("The problem does not have solution");
            return;
        }

        double[] bound = matrixVectorMultiply(A, x0);
        for (int i = 0; i < A.length; i++) {
            if (bound[i] > b[i]) {
                System.out.println("The method is not applicable");
                return;
            }
        }

        double[] x = Arrays.copyOf(x0, x0.length);
        int counter = 0;
        while (true) {
            counter++;
            double[] D = diagonalMatrix(x);
            double[][] A_tilda = matrixMultiply(A, diagonalToMatrix(D));
            double[] c_tilda = vectorMultiply(D, C);
            double[][] A_rev = matrixMultiply(A_tilda, transpose(A_tilda));
            double[][] mult = matrixMultiply(transpose(A_tilda),matrixMultiply(inverse(matrixMultiply(A_tilda, transpose(A_tilda))),A_tilda));
            double[][] P = subtractMatrices(identityMatrix(mult.length), mult);
            double[] c_p = matrixVectorMultiply(P, c_tilda);
            double minimum = Arrays.stream(c_p).min().getAsDouble();

            if (minimum >= 0.1) {
                System.out.println("The method is not applicable!");
                return;
            }

            double v = Math.abs(minimum);
            double[] x_tilda = addVectors(scalarMultiply(alpha / v, c_p), 1);
            double[] x_star = vectorMultiply(D, x_tilda);

            if (counter == 100) {
                break;
            }

            double da = norm(subtractVectors(x_star, x));
            if (da <= eps) {
                System.out.println("Solution for α = " + alpha + ": " + Arrays.toString(x_star));
                double objectiveValue = dotProduct(C, x_star);
                System.out.println("Objective function value: " + objectiveValue);
                return;
            }

            x = x_star;
        }

        System.out.println("The problem does not have a solution!");
    }

    public static class Tableau {
        private final double[][] data;

        public Tableau(double[][] data) {
            this.data = data;
        }

        public double getElement(int row, int col) {
            return data[row][col];
        }
    }

    public static Tableau simplexMethod(double[] C, double[][] A, double[] B, int numVariables, int numConstraints) {
        double[][] tableau = initializeTableau(C, A, B, numVariables, numConstraints);

        while (true) {
            int enteringVariable = findEnteringVariable(tableau);
            if (enteringVariable == -1) {
                Tableau result = new Tableau(tableau);
                float[] solution = new float[numVariables];
                for (int i = 1; i <= numConstraints; i++) {
                    for (int j = 0; j < numVariables; j++) {
                        if (result.getElement(i, j) == 1) {
                            solution[j] = (float) result.getElement(i, numVariables + numConstraints);
                            break;
                        }
                    }
                }

                System.out.println("Optimal solution x: " + Arrays.toString(solution));
                System.out.println("Objective function value: " + result.getElement(0, numVariables + numConstraints));
                return new Tableau(tableau);
            }

            int leavingVariable = findLeavingVariable(tableau, enteringVariable);
            if (leavingVariable == -1) {
                return null;
            }

            pivot(tableau, leavingVariable, enteringVariable);
        }
    }

    private static double[][] initializeTableau(double[] C, double[][] A, double[] B, int numVariables, int numConstraints) {
        double[][] tableau = new double[numConstraints + 1][numVariables + numConstraints + 1];

        for (int i = 0; i < numVariables; i++) {
            tableau[0][i] = -C[i];
        }

        for (int i = 0; i < numConstraints; i++) {
            for (int j = 0; j < numVariables; j++) {
                tableau[i + 1][j] = A[i][j];
            }
        }

        for (int i = 0; i < numConstraints; i++) {
            tableau[i + 1][numVariables + i] = 1;
        }

        for (int i = 0; i < numConstraints; i++) {
            tableau[i + 1][numVariables + numConstraints] = B[i];
        }

        return tableau;
    }
    private static int findEnteringVariable(double[][] tableau) {
        int enteringVariable = -1;
        double minCoefficient = 0;
        for (int i = 0; i < tableau[0].length - 1; i++) {
            if (tableau[0][i] < minCoefficient) {
                minCoefficient = tableau[0][i];
                enteringVariable = i;
            }
        }
        return enteringVariable;
    }

    private static int findLeavingVariable(double[][] tableau, int enteringVariable) {
        int leavingVariable = -1;
        double minRatio = Double.MAX_VALUE;
        for (int i = 1; i < tableau.length; i++) {
            if (tableau[i][enteringVariable] > EPSILON) {
                double ratio = tableau[i][tableau[i].length - 1] / tableau[i][enteringVariable];
                if (ratio < minRatio) {
                    minRatio = ratio;
                    leavingVariable = i;
                }
            }
        }
        return leavingVariable;
    }

    private static void pivot(double[][] tableau, int leavingVariable, int enteringVariable) {
        double pivotElement = tableau[leavingVariable][enteringVariable];

        for (int j = 0; j < tableau[leavingVariable].length; j++) {
            tableau[leavingVariable][j] /= pivotElement;
        }

        for (int i = 0; i < tableau.length; i++) {
            if (i != leavingVariable) {
                double factor = tableau[i][enteringVariable];
                for (int j = 0; j < tableau[i].length; j++) {
                    tableau[i][j] -= factor * tableau[leavingVariable][j];
                }
            }
        }
    }

    // Helper methods for matrix and vector operations
    public static double[] diagonalMatrix(double[] vector) {
        double[] diagonal = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            diagonal[i] = vector[i];
        }
        return diagonal;
    }

    public static double[][] diagonalToMatrix(double[] diagonal) {
        int n = diagonal.length;
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            matrix[i][i] = diagonal[i];
        }
        return matrix;
    }

    public static double[][] matrixMultiply(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int colsB = B[0].length;
        double[][] result = new double[rowsA][colsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return result;
    }

    public static double[] matrixVectorMultiply(double[][] A, double[] x) {
        int rowsA = A.length;
        int colsA = A[0].length;
        double[] result = new double[rowsA];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
                result[i] += A[i][j] * x[j];
            }
        }
        return result;
    }

    public static double[][] transpose(double[][] A) {
        int rows = A.length;
        int cols = A[0].length;
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = A[i][j];
            }
        }
        return result;
    }

    public static double[][] inverse(double[][] A) {
        int n = A.length;
        double[][] augmentedMatrix = new double[n][2 * n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmentedMatrix[i][j] = A[i][j];
            }
            augmentedMatrix[i][i + n] = 1;
        }

        for (int i = 0; i < n; i++) {
            double diag = augmentedMatrix[i][i];
            for (int j = 0; j < 2 * n; j++) {
                augmentedMatrix[i][j] /= diag;
            }

            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = augmentedMatrix[k][i];
                    for (int j = 0; j < 2 * n; j++) {
                        augmentedMatrix[k][j] -= factor * augmentedMatrix[i][j];
                    }
                }
            }
        }

        double[][] inverseMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inverseMatrix[i][j] = augmentedMatrix[i][j + n];
            }
        }
        return inverseMatrix;
    }

    public static double[][] identityMatrix(int n) {
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++) {
            result[i][i] = 1;
        }
        return result;
    }

    public static double[] subtractVectors(double[] a, double[] b) {
        int n = a.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    public static double[][] subtractMatrices(double[][] A, double[][] B) {
        int rows = A.length;
        int cols = A[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = A[i][j] - B[i][j];
            }
        }
        return result;
    }

    public static double[] scalarMultiply(double scalar, double[] vector) {
        int n = vector.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = scalar * vector[i];
        }
        return result;
    }

    public static double[] addVectors(double[] a, double scalar) {
        int n = a.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = a[i] + scalar;
        }
        return result;
    }

    public static double norm(double[] vector) {
        double sum = 0;
        for (double value : vector) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }

    public static double dotProduct(double[] a, double[] b) {
        int n = a.length;
        double result = 0;
        for (int i = 0; i < n; i++) {
            result += a[i] * b[i];
        }
        return result;
    }

    public static double[] vectorMultiply(double[] a, double[] b) {
        int n = a.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = a[i] * b[i];
        }
        return result;
    }
}