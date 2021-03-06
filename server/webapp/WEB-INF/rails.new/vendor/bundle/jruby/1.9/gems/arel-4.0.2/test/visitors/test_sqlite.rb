require 'helper'

module Arel
  module Visitors
    describe 'the sqlite visitor' do
      before do
        @visitor = SQLite.new Table.engine.connection_pool
      end

      it 'defaults limit to -1' do
        stmt = Nodes::SelectStatement.new
        stmt.offset = Nodes::Offset.new(1)
        sql = @visitor.accept(stmt)
        sql.must_be_like "SELECT LIMIT -1 OFFSET 1"
      end

      it 'does not support locking' do
        node = Nodes::Lock.new(Arel.sql('FOR UPDATE'))
        @visitor.accept(node).must_be_nil
      end
    end
  end
end
