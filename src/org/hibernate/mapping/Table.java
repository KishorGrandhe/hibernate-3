//$Id: Table.java 10725 2006-11-06 14:35:20Z max.andersen@jboss.com $
package org.hibernate.mapping;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.SequencedHashMap;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.Mapping;
import org.hibernate.tool.hbm2ddl.ColumnMetadata;
import org.hibernate.tool.hbm2ddl.TableMetadata;
import org.hibernate.util.CollectionHelper;

/**
 * A relational table
 *
 * @author Gavin King
 */
public class Table implements RelationalModel, Serializable {

	private String name;
	private String schema;
	private String catalog;
	/**
	 * contains all columns, including the primary key
	 */
	private Map columns = new SequencedHashMap();
	private KeyValue idValue;
	private PrimaryKey primaryKey;
	private Map indexes = new HashMap();
	private Map foreignKeys = new HashMap();
	private Map uniqueKeys = new HashMap();
	private final int uniqueInteger;
	private boolean quoted;
	private boolean schemaQuoted;
	private static int tableCounter = 0;
	private List checkConstraints = new ArrayList();
	private String rowId;
	private String subselect;
	private boolean isAbstract;
	private boolean hasDenormalizedTables = false;
	private String comment;

	//code added for pseudoColumns needed for triggers
	public Map pseudoColumns = new SequencedHashMap();

	static class ForeignKeyKey implements Serializable {
		String referencedClassName;
		List columns;
		List referencedColumns;

		ForeignKeyKey(List columns, String referencedClassName, List referencedColumns) {
			this.referencedClassName = referencedClassName;
			this.columns = new ArrayList();
			this.columns.addAll( columns );
			if ( referencedColumns != null ) {
				this.referencedColumns = new ArrayList();
				this.referencedColumns.addAll( referencedColumns );
			}
			else {
				this.referencedColumns = CollectionHelper.EMPTY_LIST;
			}
		}

		public int hashCode() {
			return columns.hashCode() + referencedColumns.hashCode();
		}

		public boolean equals(Object other) {
			ForeignKeyKey fkk = (ForeignKeyKey) other;
			return fkk.columns.equals( columns ) &&
					fkk.referencedClassName.equals( referencedClassName ) && fkk.referencedColumns
					.equals( referencedColumns );
		}
	}

	public Table() {
		uniqueInteger = tableCounter++;
	}

	public Table(String name) {
		this();
		setName( name );
	}

	public String getQualifiedName(Dialect dialect, String defaultCatalog, String defaultSchema) {
		if ( subselect != null ) {
			return "( " + subselect + " )";
		}
		String quotedName = getQuotedName( dialect );
		String usedSchema = schema == null ?
				defaultSchema :
				getQuotedSchema( dialect );
		String usedCatalog = catalog == null ?
				defaultCatalog :
				catalog;
		return qualify( usedCatalog, usedSchema, quotedName );
	}

	public static String qualify(String catalog, String schema, String table) {
		StringBuffer qualifiedName = new StringBuffer();
		if ( catalog != null ) {
			qualifiedName.append( catalog ).append( '.' );
		}
		if ( schema != null ) {
			qualifiedName.append( schema ).append( '.' );
		}
		return qualifiedName.append( table ).toString();
	}

	public String getName() {
		return name;
	}

	/**
	 * returns quoted name as it would be in the mapping file.
	 */
	public String getQuotedName() {
		return quoted ?
				"`" + name + "`" :
				name;
	}

	public String getQuotedName(Dialect dialect) {
		return quoted ?
				dialect.openQuote() + name + dialect.closeQuote() :
				name;
	}

	/**
	 * returns quoted name as it is in the mapping file.
	 */
	public String getQuotedSchema() {
		return schemaQuoted ?
				"`" + schema + "`" :
				schema;
	}

	public String getQuotedSchema(Dialect dialect) {
		return schemaQuoted ?
				dialect.openQuote() + schema + dialect.closeQuote() :
				schema;
	}

	public void setName(String name) {
		if ( name.charAt( 0 ) == '`' ) {
			quoted = true;
			this.name = name.substring( 1, name.length() - 1 );
		}
		else {
			this.name = name;
		}
	}

	/**
	 * Return the column which is identified by column provided as argument.
	 *
	 * @param column column with atleast a name.
	 * @return the underlying column or null if not inside this table. Note: the instance *can* be different than the input parameter, but the name will be the same.
	 */
	public Column getColumn(Column column) {
		if ( column == null ) {
			return null;
		}

		Column myColumn = (Column) columns.get( column.getCanonicalName() );

		return column.equals( myColumn ) ?
				myColumn :
				null;
	}

	public Column getColumn(int n) {
		Iterator iter = columns.values().iterator();
		for ( int i = 0; i < n - 1; i++ ) {
			iter.next();
		}
		return (Column) iter.next();
	}

	public void addColumn(Column column) {
		Column old = (Column) getColumn( column );
		if ( old == null ) {
			columns.put( column.getCanonicalName(), column );
			column.uniqueInteger = columns.size();
		}
		else {
			column.uniqueInteger = old.uniqueInteger;
		}
	}

	public int getColumnSpan() {
		return columns.size();
	}

	public Iterator getColumnIterator() {
		return columns.values().iterator();
	}

	public Iterator getIndexIterator() {
		return indexes.values().iterator();
	}

	public Iterator getForeignKeyIterator() {
		return foreignKeys.values().iterator();
	}

	public Iterator getUniqueKeyIterator() {
		return getUniqueKeys().values().iterator();
	}

	Map getUniqueKeys() {
		if ( uniqueKeys.size() > 1 ) {
			//deduplicate unique constraints sharing the same columns
			//this is needed by Hibernate Annotations since it creates automagically
			// unique constraints for the user
			Iterator it = uniqueKeys.entrySet().iterator();
			Map finalUniqueKeys = new HashMap( uniqueKeys.size() );
			while ( it.hasNext() ) {
				Map.Entry entry = (Map.Entry) it.next();
				UniqueKey uk = (UniqueKey) entry.getValue();
				List columns = uk.getColumns();
				int size = finalUniqueKeys.size();
				boolean skip = false;
				Iterator tempUks = finalUniqueKeys.entrySet().iterator();
				while ( tempUks.hasNext() ) {
					final UniqueKey currentUk = (UniqueKey) ( (Map.Entry) tempUks.next() ).getValue();
					if ( currentUk.getColumns().containsAll( columns ) && columns
							.containsAll( currentUk.getColumns() ) ) {
						skip = true;
						break;
					}
				}
				if ( !skip ) finalUniqueKeys.put( entry.getKey(), uk );
			}
			return finalUniqueKeys;
		}
		else {
			return uniqueKeys;
		}
	}

	public void validateColumns(Dialect dialect, Mapping mapping, TableMetadata tableInfo) {
		Iterator iter = getColumnIterator();
		while ( iter.hasNext() ) {
			Column col = (Column) iter.next();

			ColumnMetadata columnInfo = tableInfo.getColumnMetadata( col.getName() );

			if ( columnInfo == null ) {
				throw new HibernateException( "Missing column: " + col.getName() + " in " + Table.qualify( tableInfo.getCatalog(), tableInfo.getSchema(), tableInfo.getName()));
			}
			else {
				final boolean typesMatch = col.getSqlType( dialect, mapping )
						.startsWith( columnInfo.getTypeName().toLowerCase() )
						|| columnInfo.getTypeCode() == col.getSqlTypeCode( mapping );
				if ( !typesMatch ) {
					throw new HibernateException(
							"Wrong column type: " + col.getName() +
									", expected: " + col.getSqlType( dialect, mapping )
					);
				}
			}
		}

	}

	public Iterator sqlAlterStrings(Dialect dialect, Mapping p, TableMetadata tableInfo, String defaultCatalog,
									String defaultSchema)
			throws HibernateException {

		StringBuffer root = new StringBuffer();
		root.append(dialect.toString());
		root = root.append( "alter table " )
				.append( getQualifiedName( dialect, defaultCatalog, defaultSchema ) )
				.append( ' ' )
				.append( dialect.getAddColumnString() );

		Iterator iter = getColumnIterator();
		List results = new ArrayList();
		while ( iter.hasNext() ) {
			Column column = (Column) iter.next();

			ColumnMetadata columnInfo = tableInfo.getColumnMetadata( column.getName() );

			if ( columnInfo == null ) {
				// the column doesnt exist at all.
				StringBuffer alter = new StringBuffer( root.toString() )
						.append( ' ' )
						.append( column.getQuotedName( dialect ) )
						.append( ' ' )
						.append( column.getSqlType( dialect, p ) );

				String defaultValue = column.getDefaultValue();
				if ( defaultValue != null ) {
					alter.append( " default " ).append( defaultValue );

					if ( column.isNullable() ) {
						alter.append( dialect.getNullColumnString() );
					}
					else {
						alter.append( " not null" );
					}

				}

				boolean useUniqueConstraint = column.isUnique() &&
						dialect.supportsUnique() &&
						( !column.isNullable() || dialect.supportsNotNullUnique() );
				if ( useUniqueConstraint ) {
					alter.append( " unique" );
				}

				if ( column.hasCheckConstraint() && dialect.supportsColumnCheck() ) {
					alter.append( " check(" )
							.append( column.getCheckConstraint() )
							.append( ")" );
				}

				String columnComment = column.getComment();
				if ( columnComment != null ) {
					alter.append( dialect.getColumnComment( columnComment ) );
				}

				results.add( alter.toString() );
			}

		}

		return results.iterator();
	}

	public boolean hasPrimaryKey() {
		return getPrimaryKey() != null;
	}

	public String sqlTemporaryTableCreateString(Dialect dialect, Mapping mapping) throws HibernateException {
		StringBuffer buffer = new StringBuffer( dialect.getCreateTemporaryTableString() )
				.append( ' ' )
				.append( name )
				.append( " (" );
		Iterator itr = getColumnIterator();
		while ( itr.hasNext() ) {
			final Column column = (Column) itr.next();
			buffer.append( column.getQuotedName( dialect ) ).append( ' ' );
			buffer.append( column.getSqlType( dialect, mapping ) );
			if ( column.isNullable() ) {
				buffer.append( dialect.getNullColumnString() );
			}
			else {
				buffer.append( " not null" );
			}
			if ( itr.hasNext() ) {
				buffer.append( ", " );
			}
		}
		buffer.append( ") " );
		buffer.append( dialect.getCreateTemporaryTablePostfix() );
		return buffer.toString();
	}

	public String sqlCreateString(Dialect dialect, Mapping p, String defaultCatalog, String defaultSchema)
			throws HibernateException {

		StringBuffer buf = new StringBuffer( "create table " )
				.append( getQualifiedName( dialect, defaultCatalog, defaultSchema ) )
				.append( " (" );

		boolean identityColumn = idValue != null && idValue.isIdentityColumn( dialect );

		// Try to find out the name of the primary key to create it as identity if the IdentityGenerator is used
		String pkname = null;
		if ( hasPrimaryKey() && identityColumn ) {
			pkname = ( (Column) getPrimaryKey().getColumnIterator().next() ).getQuotedName( dialect );
		}

		Iterator iter = getColumnIterator();
		while ( iter.hasNext() ) {
			Column col = (Column) iter.next();

			buf.append( col.getQuotedName( dialect ) )
					.append( ' ' );

			if ( identityColumn && col.getQuotedName( dialect ).equals( pkname ) ) {
				// to support dialects that have their own identity data type
				if ( dialect.hasDataTypeInIdentityColumn() ) {
					buf.append( col.getSqlType( dialect, p ) );
				}
				buf.append( ' ' )
						.append( dialect.getIdentityColumnString( col.getSqlTypeCode( p ) ) );
			}
			else {

				buf.append( col.getSqlType( dialect, p ) );

				String defaultValue = col.getDefaultValue();
				if ( defaultValue != null ) {
					buf.append( " default " ).append( defaultValue );
				}

				if ( col.isNullable() ) {
					buf.append( dialect.getNullColumnString() );
				}
				else {
					buf.append( " not null" );
				}

			}

			boolean useUniqueConstraint = col.isUnique() &&
					( !col.isNullable() || dialect.supportsNotNullUnique() );
			if ( useUniqueConstraint ) {
				if ( dialect.supportsUnique() ) {
					buf.append( " unique" );
				}
				else {
					UniqueKey uk = getOrCreateUniqueKey( col.getQuotedName( dialect ) + '_' );
					uk.addColumn( col );
				}
			}

			if ( col.hasCheckConstraint() && dialect.supportsColumnCheck() ) {
				buf.append( " check (" )
						.append( col.getCheckConstraint() )
						.append( ")" );
			}

			String columnComment = col.getComment();
			if ( columnComment != null ) {
				buf.append( dialect.getColumnComment( columnComment ) );
			}

			if ( iter.hasNext() ) {
				buf.append( ", " );
			}

		}

		//code added for adding pk's on middle tables start
		boolean pkAdded = false;
		if (getName().indexOf("_") > 0 )
		{
			//Only middle / join tables have "_" in the name, table always have 2 columns
			Iterator columnIter = getColumnIterator();
			if (columnIter != null)
			{
				PrimaryKey pk = new PrimaryKey();
				int i = 0;
				while (columnIter.hasNext())
				{
					pk.addColumn((Column)columnIter.next());
					i++;
				}
				if (i == 2)
				{
					setPrimaryKey(pk);
					pkAdded = true;
				}
			}
		}
		//code added for adding pk's on middle tables end

		if ( hasPrimaryKey() ) {
			buf.append( ", " )
					.append( getPrimaryKey().sqlConstraintString( dialect ) );
		}
		//code added for setting pkey start
		if (pkAdded)
		{
			setPrimaryKey(null);
		}
		//code added for setting pkey end
		if ( dialect.supportsUniqueConstraintInCreateAlterTable() ) {

			Iterator ukiter = getUniqueKeyIterator();
			while ( ukiter.hasNext() ) {
				UniqueKey uk = (UniqueKey) ukiter.next();
				String a = uk.getTable().getName()+","+ uk.getName();
				try
				{
					PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(".\\build\\uniqueConstraints.csv"),true)),true);

					//Only middle / join tables have "_" in the name, table always have 2 columns

					if (getName().indexOf("_") > 0 && uk.getColumns().size() == 2  )
					{
						continue;
					}
					for (int i=0;i<uk.getColumns().size();i++)
					{
						a+=","+uk.getColumn(i).getName()+","+uk.getColumn(i).isNullable();
					}
					ps.println(a);
				}catch(Exception e)
				{
					e.printStackTrace();
				}


				UniqueKey ukx = new UniqueKey();
				ukx.setName( uk.getName());
				ukx.setTable( this );

				int nullCount = 0;
				int ukColumnCount = 0;
				Iterator itr = uk.getColumnIterator();
				while (itr.hasNext())
				{
					Column col = (Column)itr.next();
					if (col.isNullable())
					{
						nullCount++;
						if (nullCount > 1)
						{
							break;
						}
					}
					++ukColumnCount;
				}
				if (getName().indexOf("_") > 0 && ukColumnCount == 2)
				{
					continue;
				}
				itr = uk.getColumnIterator();
				while (itr.hasNext())
				{
					Column col = (Column)itr.next();
					if (col.isNullable())
					{
						//Column colClone = (Column)pseudoColumns.get(col.getName());
						HashMap columns = (HashMap)pseudoColumns.get(col.getName());
						Column colClone = null;
						String colType = nullCount > 1?"NAV":"NNV";
						if (columns == null)
						{
							columns = new HashMap();
							pseudoColumns.put(col.getName(),columns);
						}
						colClone = (Column)columns.get(colType);
						//need to add pseudo column
						if (colClone == null)
						{
							colClone = (Column)col.clone();
							addColumn(colClone);
							colClone.setName(col.getName()+(colType.equals("NAV")?("_"+colType.toLowerCase()):"")+"_pxy");
							colClone.setNullable(false);
							buf.append( ", " );
							buf.append( colClone.getQuotedName( dialect ) ).append( ' ' );
							buf.append( colClone.getSqlType( dialect, p ) );
							buf.append( " not null " );

							if (dialect instanceof DB2Dialect)
							{
								if (nullCount > 1)
								{
									buf.append("generated always as (coalesce("+col.getName()+", \'pxy\'))");
								}
								else
								{
									//buf.append("generated always as (case when "+col.getName() +" is not null then "+col.getName()+" ELSE id end)");
									buf.append("generated always as (coalesce("+col.getName()+", id))");
								}
							}
							if (dialect instanceof MySQLDialect)
							{
								buf.append(" default \"\"");
							}
							columns.put(colType, colClone);
						}
						ukx.addColumn(colClone);
					}
					else
					{
						ukx.addColumn(col);
					}
				}
				uk = ukx;
				String constraint = uk.sqlConstraintString( dialect );
				if ( constraint != null ) {
					buf.append( ", " ).append( constraint );
				}
			}
		}
		/*Iterator idxiter = getIndexIterator();
		while ( idxiter.hasNext() ) {
			Index idx = (Index) idxiter.next();
			buf.append(',').append( idx.sqlConstraintString(dialect) );
		}*/

		if ( dialect.supportsTableCheck() ) {
			Iterator chiter = checkConstraints.iterator();
			while ( chiter.hasNext() ) {
				buf.append( ", check (" )
						.append( chiter.next() )
						.append( ')' );
			}
		}

		buf.append( ')' );

		if ( comment != null ) {
			buf.append( dialect.getTableComment( comment ) );
		}

		return buf.append( dialect.getTableTypeString() ).toString();
	}

	public String sqlDropString(Dialect dialect, String defaultCatalog, String defaultSchema) {
		StringBuffer buf = new StringBuffer( "drop table " );
		if ( dialect.supportsIfExistsBeforeTableName() ) {
			buf.append( "if exists " );
		}
		buf.append( getQualifiedName( dialect, defaultCatalog, defaultSchema ) )
				.append( dialect.getCascadeConstraintsString() );
		if ( dialect.supportsIfExistsAfterTableName() ) {
			buf.append( " if exists" );
		}
		return buf.toString();
	}

	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	public Index getOrCreateIndex(String indexName) {

		Index index = (Index) indexes.get( indexName );

		if ( index == null ) {
			index = new Index();
			index.setName( indexName );
			index.setTable( this );
			indexes.put( indexName, index );
		}

		return index;
	}

	public Index getIndex(String indexName) {
		return (Index) indexes.get( indexName );
	}

	public Index addIndex(Index index) {
		Index current = (Index) indexes.get( index.getName() );
		if ( current != null ) {
			throw new MappingException( "Index " + index.getName() + " already exists!" );
		}
		indexes.put( index.getName(), index );
		return index;
	}

	public UniqueKey addUniqueKey(UniqueKey uniqueKey) {
		UniqueKey current = (UniqueKey) uniqueKeys.get( uniqueKey.getName() );
		if ( current != null ) {
			throw new MappingException( "UniqueKey " + uniqueKey.getName() + " already exists!" );
		}
		uniqueKeys.put( uniqueKey.getName(), uniqueKey );
		return uniqueKey;
	}

		private String getShortName(String name,int length )
		{
			String keyName = "";
			//String str= name.replaceAll("[AEIOUaeiou]", "");
			String[] ccNames = name.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
			for (int i=0;i<ccNames.length;i++)
			{
				String ccName = ccNames[i];
				ccName = ccName.substring(0,1) +ccName.substring(1).replaceAll("[AEIOUaeiou_]", "");
				if (ccName.length() > length)
				{
					keyName+=ccName.substring(0,length);
				}
				else
				{
					keyName+=ccName;
				}
			}
			return keyName;
		}
	public UniqueKey createUniqueKey(List keyColumns) {
//		String keyName = "UK_" + uniqueColumnString( keyColumns.iterator() );
		String keyName = "";
		boolean needsUnderScore = false;
		Iterator iter = keyColumns.iterator();
		while (iter.hasNext())
		//for (Column col:keyColumns)
		{
			Column col = (Column)iter.next();
			String colName = col.getName();
			if (needsUnderScore)
				keyName+="_";
			keyName +=getShortName(colName,3);
			needsUnderScore = true;
		}

		int tabNameLength = 59 - keyName.length();
		String tabName = getName();
		//log.info("Table Name ="+tabName+", Tablename Length = "+tabName.length()+", Needed Tablename Length= "+tabNameLength+", keyName = "+keyName);

		if (getName().length() > tabNameLength)
		{
			tabName = getName().substring(0,tabNameLength-1);
		}
		keyName = "UK_"+tabName+"$"+keyName;

		if (keyName.length() > 64)
		{
			keyName = keyName.substring(0,63);
		}
		UniqueKey uk = getOrCreateUniqueKey( keyName );
		uk.addColumns( keyColumns.iterator() );
		return uk;
	}

	public UniqueKey getUniqueKey(String keyName) {
		return (UniqueKey) uniqueKeys.get( keyName );
	}

	public UniqueKey getOrCreateUniqueKey(String keyName) {
		UniqueKey uk = (UniqueKey) uniqueKeys.get( keyName );

		if ( uk == null ) {
			uk = new UniqueKey();
			uk.setName( keyName );
			uk.setTable( this );
			uniqueKeys.put( keyName, uk );
		}
		return uk;
	}

	public void createForeignKeys() {
	}

	public ForeignKey createForeignKey(String keyName, List keyColumns, String referencedEntityName) {
		return createForeignKey( keyName, keyColumns, referencedEntityName, null );
	}

	public ForeignKey createForeignKey(String keyName, List keyColumns, String referencedEntityName,
									   List referencedColumns) {
		Object key = new ForeignKeyKey( keyColumns, referencedEntityName, referencedColumns );

		ForeignKey fk = (ForeignKey) foreignKeys.get( key );
		if ( fk == null ) {
			fk = new ForeignKey();
			if ( keyName != null ) {
				fk.setName( keyName );
			}
			else {
				fk.setName( "FK" + uniqueColumnString( keyColumns.iterator(), referencedEntityName ) );
				//TODO: add referencedClass to disambiguate to FKs on the same
				//      columns, pointing to different tables
			}
			fk.setTable( this );
			foreignKeys.put( key, fk );
			fk.setReferencedEntityName( referencedEntityName );
			fk.addColumns( keyColumns.iterator() );
			if ( referencedColumns != null ) {
				fk.addReferencedColumns( referencedColumns.iterator() );
			}
		}

		if ( keyName != null ) {
			fk.setName( keyName );
		}

		return fk;
	}


	public String uniqueColumnString(Iterator iterator) {
		return uniqueColumnString( iterator, null );
	}

	public String uniqueColumnString(Iterator iterator, String referencedEntityName) {
//		int result = 0;
//		if ( referencedEntityName != null ) {
//			result += referencedEntityName.hashCode();
//		}
//		while ( iterator.hasNext() ) {
//			result += iterator.next().hashCode();
//		}
//		return ( Integer.toHexString( name.hashCode() ) + Integer.toHexString( result ) ).toUpperCase();
		String keyName = "";
		boolean needsUnderScore = false;
		Iterator iter = iterator;
		while (iter.hasNext())
		//for (Column col:keyColumns)
		{
			Column col = (Column)iter.next();
			String colName = col.getName();
			if (needsUnderScore)
				keyName+="_";
			keyName +=getShortName(colName,6);
			needsUnderScore = true;
		}

		int tabNameLength = 59 - keyName.length();
		String tabName = getName();
		//log.info("Table Name ="+tabName+", Tablename Length = "+tabName.length()+", Needed Tablename Length= "+tabNameLength+", keyName = "+keyName);

		if (getName().length() > tabNameLength)
		{
			tabName = getName().substring(0,tabNameLength-1);
		}
		keyName = "_"+tabName+"$"+keyName;

		if (keyName.length() > 61)
		{
			keyName = keyName.substring(0,60);
		}

		return keyName;
	}


	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		if ( schema != null && schema.charAt( 0 ) == '`' ) {
			schemaQuoted = true;
			this.schema = schema.substring( 1, schema.length() - 1 );
		}
		else {
			this.schema = schema;
		}
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public int getUniqueInteger() {
		return uniqueInteger;
	}

	public void setIdentifierValue(KeyValue idValue) {
		this.idValue = idValue;
	}

	public KeyValue getIdentifierValue() {
		return idValue;
	}

	public boolean isSchemaQuoted() {
		return schemaQuoted;
	}

	public boolean isQuoted() {
		return quoted;
	}

	public void setQuoted(boolean quoted) {
		this.quoted = quoted;
	}

	public void addCheckConstraint(String constraint) {
		checkConstraints.add( constraint );
	}

	public boolean containsColumn(Column column) {
		return columns.containsValue( column );
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer().append( getClass().getName() )
				.append( '(' );
		if ( getCatalog() != null ) {
			buf.append( getCatalog() + "." );
		}
		if ( getSchema() != null ) {
			buf.append( getSchema() + "." );
		}
		buf.append( getName() ).append( ')' );
		return buf.toString();
	}

	public String getSubselect() {
		return subselect;
	}

	public void setSubselect(String subselect) {
		this.subselect = subselect;
	}

	public boolean isSubselect() {
		return subselect != null;
	}

	public boolean isAbstractUnionTable() {
		return hasDenormalizedTables() && isAbstract;
	}

	public boolean hasDenormalizedTables() {
		return hasDenormalizedTables;
	}

	void setHasDenormalizedTables() {
		hasDenormalizedTables = true;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public boolean isPhysicalTable() {
		return !isSubselect() && !isAbstractUnionTable();
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Iterator getCheckConstraintsIterator() {
		return checkConstraints.iterator();
	}

	public Iterator sqlCommentStrings(Dialect dialect, String defaultCatalog, String defaultSchema) {
		List comments = new ArrayList();
		if ( dialect.supportsCommentOn() ) {
			String tableName = getQualifiedName( dialect, defaultCatalog, defaultSchema );
			if ( comment != null ) {
				StringBuffer buf = new StringBuffer()
						.append( "comment on table " )
						.append( tableName )
						.append( " is '" )
						.append( comment )
						.append( "'" );
				comments.add( buf.toString() );
			}
			Iterator iter = getColumnIterator();
			while ( iter.hasNext() ) {
				Column column = (Column) iter.next();
				String columnComment = column.getComment();
				if ( columnComment != null ) {
					StringBuffer buf = new StringBuffer()
							.append( "comment on column " )
							.append( tableName )
							.append( '.' )
							.append( column.getQuotedName( dialect ) )
							.append( " is '" )
							.append( columnComment )
							.append( "'" );
					comments.add( buf.toString() );
				}
			}
		}
		return comments.iterator();
	}

}
