--
-- This file is extracted manually from opendcs.sql, which is generated by ERM.
--
-- A shell script expands this template by copying it and renaming the 0000
-- suffix with however many files the user has selected.
-- Note that tables 0000 are created by the opendcs.sql

CREATE TABLE TS_NUM_0000
(
	TS_ID INT NOT NULL,
	SAMPLE_TIME NUMBER(19) NOT NULL,
	TS_VALUE DOUBLE PRECISION NOT NULL,
	-- Bitwise flags for each value
	FLAGS NUMBER(19) NOT NULL,
	SOURCE_ID INT NOT NULL,
	DATA_ENTRY_TIME NUMBER(19) NOT NULL,
	PRIMARY KEY (TS_ID, SAMPLE_TIME)
) &TBL_SPACE_SPEC;

ALTER TABLE TS_NUM_0000
	ADD CONSTRAINT TS_NUM_0000_FKSR
	FOREIGN KEY (SOURCE_ID)
	REFERENCES TSDB_DATA_SOURCE (SOURCE_ID)
;

ALTER TABLE TS_NUM_0000
	ADD CONSTRAINT TS_NUM_0000_FKTS
	FOREIGN KEY (TS_ID)
	REFERENCES TS_SPEC (TS_ID)
;

CREATE INDEX TS_NUM_0000_ENTRY_IDX 
	ON TS_NUM_0000(DATA_ENTRY_TIME) &TBL_SPACE_SPEC;