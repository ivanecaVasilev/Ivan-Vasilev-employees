import * as React from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';


const DataTable = ({files}) => {
    const tableStyle={padding:'15px 10px', width:'100%', margin:'5px auto'};

    return (
        <TableContainer component={Paper} >
            <Table sx={{minWidth: 250}} aria-label="simple table" style={tableStyle}>
                <TableHead>
                    <TableRow>
                        <TableCell>EmployeeOneId</TableCell>
                        <TableCell>EmployeeTwoId</TableCell>
                        <TableCell>ProjectId</TableCell>
                        <TableCell>Days</TableCell>
                        {/*<TableCell align="right">Protein&nbsp;(g)</TableCell>*/}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {files?.map((file) => (
                        <TableRow
                            key={file.employeeOneId + " and " + file.employeeTwoId + " and " + file.projectId}
                            sx={{'&:last-child td, &:last-child th': {border: 0}}}
                        >
                            <TableCell component="th" scope="row">
                                {file.employeeOneId}
                            </TableCell>
                            <TableCell >{file.employeeTwoId}</TableCell>
                            <TableCell >{file.projectId}</TableCell>
                            <TableCell >{file.days}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
}
export default DataTable