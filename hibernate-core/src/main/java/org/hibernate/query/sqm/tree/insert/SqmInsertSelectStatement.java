/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.insert;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Incubating;
import org.hibernate.query.criteria.JpaCriteriaInsertSelect;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.SqmQuerySource;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.cte.SqmCteStatement;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.select.SqmQueryPart;
import org.hibernate.query.sqm.tree.select.SqmQuerySpec;

/**
 * @author Steve Ebersole
 */
@Incubating
public class SqmInsertSelectStatement<T> extends AbstractSqmInsertStatement<T> implements JpaCriteriaInsertSelect<T> {
	private SqmQueryPart<?> selectQueryPart;

	public SqmInsertSelectStatement(SqmRoot<T> targetRoot, NodeBuilder nodeBuilder) {
		super( targetRoot, SqmQuerySource.HQL, nodeBuilder );
		this.selectQueryPart = new SqmQuerySpec<>( nodeBuilder );
	}

	public SqmInsertSelectStatement(Class<T> targetEntity, NodeBuilder nodeBuilder) {
		super(
				new SqmRoot<>(
						nodeBuilder.getDomainModel().entity( targetEntity ),
						null,
						false,
						nodeBuilder
				),
				SqmQuerySource.CRITERIA,
				nodeBuilder
		);
		this.selectQueryPart = new SqmQuerySpec<>( nodeBuilder );
	}

	private SqmInsertSelectStatement(
			NodeBuilder builder,
			SqmQuerySource querySource,
			Set<SqmParameter<?>> parameters,
			Map<String, SqmCteStatement<?>> cteStatements,
			boolean withRecursiveCte,
			SqmRoot<T> target,
			List<SqmPath<?>> insertionTargetPaths,
			SqmQueryPart<?> selectQueryPart) {
		super( builder, querySource, parameters, cteStatements, withRecursiveCte, target, insertionTargetPaths );
		this.selectQueryPart = selectQueryPart;
	}

	@Override
	public SqmInsertSelectStatement<T> copy(SqmCopyContext context) {
		final SqmInsertSelectStatement<T> existing = context.getCopy( this );
		if ( existing != null ) {
			return existing;
		}
		return context.registerCopy(
				this,
				new SqmInsertSelectStatement<>(
						nodeBuilder(),
						getQuerySource(),
						copyParameters( context ),
						copyCteStatements( context ),
						isWithRecursive(),
						getTarget().copy( context ),
						copyInsertionTargetPaths( context ),
						selectQueryPart.copy( context )
				)
		);
	}

	public SqmQueryPart<?> getSelectQueryPart() {
		return selectQueryPart;
	}

	public void setSelectQueryPart(SqmQueryPart<?> selectQueryPart) {
		this.selectQueryPart = selectQueryPart;
	}

	@Override
	public <X> X accept(SemanticQueryWalker<X> walker) {
		return walker.visitInsertSelectStatement( this );
	}

	@Override
	public JpaPredicate getRestriction() {
		// insert has no predicate
		return null;
	}

	@Override
	public void appendHqlString(StringBuilder sb) {
		super.appendHqlString( sb );
		sb.append( ' ' );
		selectQueryPart.appendHqlString( sb );
	}
}
