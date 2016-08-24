package corp.cencosud.roble;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400JDBCDriver;


public class InicioPrograma {

	private static BufferedWriter bw;
	private static String path;

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Map <String, String> mapArguments = new HashMap<String, String>();
		String sKeyAux = null;

		for (int i = 0; i < args.length; i++) {

			if (i % 2 == 0) {

				sKeyAux = args[i];
			}
			else {

				mapArguments.put(sKeyAux, args[i]);
			}
		}

		try {

			File info              = null;
			File miDir             = new File(".");
			path                   =  miDir.getCanonicalPath();
			info                   = new File(path+"/info.txt");
			bw = new BufferedWriter(new FileWriter(info));
			info("El programa se esta ejecutando...");
			crearTxt(mapArguments);
			System.out.println("El programa finalizo.");
			info("El programa finalizo.");
			bw.close();
		}
		catch (Exception e) {

			System.out.println(e.getMessage());
		}
	}

	private static void crearTxt(Map <String, String> mapArguments) {

		Connection dbconnection = crearConexion();
		Connection dbconnOracle = crearConexionOracle();
		File file1              = null;
		BufferedWriter bw       = null;
		BufferedWriter bw2      = null;
		PreparedStatement pstmt = null;
		StringBuffer sb         = null;
		int iFechaIni           = 0;
		int iFechaFin           = 0;

		try {

			try {

				iFechaIni = restarDia(mapArguments.get("-fi"));
				iFechaFin = restarDia(mapArguments.get("-ff"));
			}
			catch (Exception e) {

				e.printStackTrace();
			}
			file1                   = new File(path + "/AjusteDeInventario-" + iFechaIni + ".txt");
			sb = new StringBuffer();
			sb.append("select ");
			sb.append("a.ibolrettsl as Indicador, ");
			sb.append("a.fectrantsl as Fecha_Transaccion, ");
			sb.append("a.numctltsl as Controlador, ");
			sb.append("a.numtertsl as Terminal, ");
			sb.append("a.numtrantsl as Transaccion, ");
			sb.append("a.numcorrdup, ");
			sb.append("cast(c.numdoceom as char(20)) as SolicitudPOS, ");
			sb.append("b.canarttsl as CantidadPOS, ");
			sb.append("d.itrgdp as SolicitudJDA, ");
			sb.append("abs(itrqty) as CantidadJDA, ");
			sb.append("e.inumbr as SKU, ");
			sb.append("e.idescr as DescripcionSKU, ");
			sb.append("e.Idept  as Depto, ");
			sb.append("e.isdept as Subdepto, ");
			sb.append("d.itrtyp as Trx_JDA, ");
			sb.append("d.itrloc as Local, ");
			sb.append("d.itrltp as OrigenLocal ");
			sb.append("from svalbugd.svvsf00 a ");
			sb.append("inner join svalbugd.svvsf01 b on a.fectrantsl = b.fectrantsl ");
			sb.append("and a.numctltsl = b.numctltsl ");
			sb.append("and a.numtertsl = b.numtertsl ");
			sb.append("and a.numtrantsl = b.numtrantsl ");
			sb.append("and a.numcorrdup = b.numcorrdup ");
			sb.append("inner join svalbugd.svlvf30 c on c.fectrantsl = b.fectrantsl ");
			sb.append("and c.numctltsl = b.numctltsl ");
			sb.append("and c.numtertsl = b.numtertsl ");
			sb.append("and c.numtrantsl = b.numtrantsl ");
			sb.append("and c.numcorrdup = b.numcorrdup ");
			sb.append("inner join mmsp4lib.invmst e on substr(b.codarttsl,3,9) = e.inumbr ");
			sb.append("left join mmsp4lib.invaud d on substr(c.numdoceom,12,9) = d.itrgdp and  substr(b.codarttsl,3,9) = d.inumbr and canarttsl = abs(itrqty) ");
			sb.append("where b.fectrantsl >= ? ");
			sb.append("and b.fectrantsl <= ? ");
			sb.append("and e.isdept not in (98, 97) ");
			sb.append("and b.mardestsl = '0' ");
			sb.append("and (d.itrltp is null or d.itrltp='W') ");
			sb.append("and e.inumbr not in (696544999) ");
			sb.append("and d.itrtyp IS NULL ");

			pstmt         = dbconnection.prepareStatement(sb.toString());
			pstmt.setInt(1, iFechaIni);
			pstmt.setInt(2, iFechaFin);
			sb = new StringBuffer();
			ResultSet rs = pstmt.executeQuery();
			bw  = new BufferedWriter(new FileWriter(file1));
			bw.write("Indicador;");
			bw.write("Fecha_Transaccion;");
			bw.write("Controlador;");
			bw.write("Terminal;");
			bw.write("Transaccion;");
			bw.write("numcorrdup;");
			bw.write("SolicitudPOS;");
			bw.write("CantidadPOS;");
			bw.write("SolicitudJDA;");
			bw.write("CantidadJDA;");
			bw.write("SKU;");
			bw.write("DescripcionSKU;");
			bw.write("Depto;");
			bw.write("Subdepto;");
			bw.write("Trx_JDA;");
			bw.write("Local;");
			bw.write("OrigenLocal;");

			bw.write("Cantidad;");
			bw.write("Cantidadsolicitada;");
			bw.write("Orden_LocalDespacho;");
			bw.write("OrderType;");
			bw.write("Estado;");
			bw.write("Estado_Linea;");
			bw.write("Confirmado\n");

			while (rs.next()) {

				bw.write(rs.getString("Indicador") + ";");
				bw.write(rs.getString("Fecha_Transaccion") + ";");
				bw.write(rs.getString("Controlador") + ";");
				bw.write(rs.getString("Terminal") + ";");
				bw.write(rs.getString("Transaccion") + ";");
				bw.write(rs.getString("numcorrdup") + ";");
				bw.write(rs.getString("SolicitudPOS") + ";");
				bw.write(rs.getString("CantidadPOS") + ";");
				bw.write(rs.getString("SolicitudJDA") + ";");
				bw.write(rs.getString("CantidadJDA") + ";");
				bw.write(rs.getString("SKU") + ";");
				bw.write(rs.getString("DescripcionSKU") + ";");
				bw.write(rs.getString("Depto") + ";");
				bw.write(rs.getString("Subdepto") + ";");
				bw.write(rs.getString("Trx_JDA") + ";");
				bw.write(rs.getString("Local") + ";");
				bw.write(rs.getString("OrigenLocal") + ";");

				if (rs.getString("SolicitudJDA") == null) {

					bw.write(ejecutarQuery2(limpiarCeros(rs.getString("SolicitudPOS")), rs.getString("SKU"), dbconnOracle));
				}
			}

			info("Archivos creados.");
		}
		catch (Exception e) {

			System.out.println(e.getMessage());
			info("[crearTxt1]Exception:"+e.getMessage());
		}
		finally {

			cerrarTodo(dbconnection,pstmt,bw);
			cerrarTodo(dbconnOracle, null, bw2);
		}
	}

	private static String ejecutarQuery2(String sSolPOS, String sSKU, Connection dbconnection) {

		StringBuffer sb         = new StringBuffer();
		PreparedStatement pstmt = null;

		try {

			sb = new StringBuffer();
			sb.append("SELECT po2.tc_purchase_orders_id as Orden_Numero, ");
			sb.append("poli2.sku as Orden_ItemSku, ");
			sb.append("poli2.ALLOCATED_QTY as Cantidad, ");
			sb.append("poli2.order_qty as Cantidadsolicitada, ");
			sb.append("max(Ao.A_Dcname) as Orden_LocalDespacho, ");
			sb.append("OT.order_type as OrderType, ");
			sb.append("POST.description as Estado, ");
			sb.append("POST2.description as Estado_Linea, ");
			sb.append("PO2.is_purchase_orders_confirmed as Confirmado ");
			sb.append("FROM CA14.purchase_orders po2 ");
			sb.append("inner JOIN CA14.purchase_orders_line_item poli2 ");
			sb.append("ON po2.purchase_orders_id = poli2.purchase_orders_id ");
			sb.append("INNER JOIN ca14.order_type OT ");
			sb.append("ON OT.order_type_id = PO2.order_category ");
			sb.append("INNER JOIN ca14.purchase_orders_status POST ");
			sb.append("ON PO2.purchase_orders_status = POST.purchase_orders_status ");
			sb.append("INNER JOIN ca14.purchase_orders_status POST2 ");
			sb.append("ON POLI2.PURCHASE_ORDERS_LINE_STATUS = POST2.purchase_orders_status ");
			sb.append("LEFT JOIN a_orderinventoryallocation AO ");
			sb.append("ON ao.a_orderlineid = poli2.purchase_orders_line_item_id ");
			sb.append("AND ao.a_skuname =  poli2.sku ");
			sb.append("WHERE PO2.TC_PURCHASE_ORDERS_ID = '");
			sb.append(sSolPOS);
			sb.append("'");
			sb.append(" GROUP BY po2.tc_purchase_orders_id, po2.created_dttm, PO2.entry_code, poli2.sku, poli2.ALLOCATED_QTY,poli2.order_QTY, OT.order_type, ");
			sb.append("POST.description, ");
			sb.append("POST2.description, ");
			sb.append("PO2.is_purchase_orders_confirmed");
			pstmt = dbconnection.prepareStatement(sb.toString());
			ResultSet rs = pstmt.executeQuery();
			sb = new StringBuffer();

			boolean reg = false;
			do{
				reg = rs.next();
				if (reg){
					sb.append(rs.getString("Cantidad") + ";");
					sb.append(rs.getString("Cantidadsolicitada") + ";");
					sb.append(rs.getString("Orden_LocalDespacho") + ";");
					sb.append(rs.getString("OrderType") + ";");
					sb.append(rs.getString("Estado") + ";");
					sb.append(rs.getString("Estado_Linea") + ";");
					sb.append(rs.getString("Confirmado") + "\n");
					break;
				}else{
					sb.append("\n");
				}
			}
			while (reg);
		}
		catch (Exception e) {

			e.printStackTrace();
			info("[crearTxt2]Exception:"+e.getMessage());
		}
		finally {

			cerrarTodo(null,pstmt,null);
		}
		return sb.toString();
	}

	private static Connection crearConexion() {

		System.out.println("Creado conexion a ROBLE.");
		AS400JDBCDriver d = new AS400JDBCDriver();
		String mySchema = "RDBPARIS2";
		Properties p = new Properties();
		AS400 o = new AS400("roble.cencosud.corp","USRCOM", "USRCOM");
		Connection dbconnection = null;

		try {

			System.out.println("AuthenticationScheme: "+o.getVersion());
			dbconnection = d.connect (o, p, mySchema);
			System.out.println("Conexion a ROBLE CREADA.");
		}
		catch (Exception e) {

			System.out.println(e.getMessage());
		}
		return dbconnection;
	}

	private static Connection crearConexionOracle() {

		Connection dbconnection = null;

		try {

			Class.forName("oracle.jdbc.driver.OracleDriver");

			//Comentado por cambio de base de datos. El servidor g500603svcr9 corresponde shareplex.
			//dbconnection = DriverManager.getConnection("jdbc:oracle:thin:@g500603svcr9:1521:MEOMCLP","REPORTER","RptCyber2015");
			
			//El servidor g500603sv0zt corresponde a Producción.
			dbconnection = DriverManager.getConnection("jdbc:oracle:thin:@g500603sv0zt:1521:MEOMCLP","ca14","Manhattan1234");
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		return dbconnection;
	}

	private static String limpiarCeros(String str) {

		int iCont = 0;

		while (str.charAt(iCont) == '0') {

			iCont++;
		}
		return str.substring(iCont, str.length());
	}

	private static void cerrarTodo(Connection cnn, PreparedStatement pstmt, BufferedWriter bw){

		try {

			if (cnn != null) {

				cnn.close();
				cnn = null;
			}
		}
		catch (Exception e) {

			System.out.println(e.getMessage());
			info("[cerrarTodo]Exception:"+e.getMessage());
		}
		try {

			if (pstmt != null) {

				pstmt.close();
				pstmt = null;
			}
		}
		catch (Exception e) {

			System.out.println(e.getMessage());
			info("[cerrarTodo]Exception:"+e.getMessage());
		}
		try {

			if (bw != null) {

				bw.flush();
				bw.close();
				bw = null;
			}
		}
		catch (Exception e) {

			System.out.println(e.getMessage());
			info("[cerrarTodo]Exception:"+e.getMessage());
		}
	}

	private static void info(String texto){

		try {

			bw.write(texto+"\n");
			bw.flush();
		}
		catch (Exception e) {

			System.out.println("Exception:"+e.getMessage());
		}
	}

	private static int restarDia(String sDia) {

		int dia = 0;
		String sFormato = "yyyyMMdd";
		Calendar diaAux = null;
		String sDiaAux = null;
		SimpleDateFormat df = null;

		try {

			diaAux = Calendar.getInstance();
			df = new SimpleDateFormat(sFormato);
			diaAux.setTime(df.parse(sDia));
			diaAux.add(Calendar.DAY_OF_MONTH, -1);
			sDiaAux = df.format(diaAux.getTime());
			dia = Integer.parseInt(sDiaAux);
		}
		catch (Exception e) {

			info("[restarDia]Exception:"+e.getMessage());
		}
		return dia;
	}
}
